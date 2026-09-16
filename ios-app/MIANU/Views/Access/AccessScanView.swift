import SwiftUI

public struct AccessScanView: View {
    @ObservedObject private var session = AppSession.shared
    @ObservedObject private var nfcReader = NFCReader.shared
    
    @State private var halls: [HallResponse] = []
    @State private var selectedHallId: String = ""
    @State private var selectedAction: AccessAction = .entry
    @State private var manualCardUid = ""
    @State private var isSubmitting = false
    @State private var lastLog: AccessLogResponse? = nil
    @State private var errorMessage: String? = nil
    
    public init() {}
    
    private var activeHall: HallResponse? {
        halls.first { $0.id == selectedHallId }
    }
    
    public var body: some View {
        NavigationView {
            ZStack {
                MianuColors.bg.ignoresSafeArea()
                
                ScrollView {
                    VStack(spacing: 20) {
                        // Header
                        HStack {
                            SectionHeader(
                                title: "Access Control",
                                eyebrow: "SESSION DOORS",
                                subtitle: "Scan smartcards at conference hall gates"
                            )
                            Spacer()
                            Button(action: { loadHalls() }) {
                                Image(systemName: "arrow.clockwise")
                                    .font(.system(size: 16))
                                    .foregroundColor(.white)
                                    .padding(10)
                                    .background(Color(hex: 0x14171E))
                                    .clipShape(Circle())
                            }
                        }
                        
                        // Hall Selector
                        VStack(alignment: .leading, spacing: 10) {
                            Text("SELECT CONFERENCE HALL")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(MianuColors.fgMuted)
                            
                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 10) {
                                    ForEach(halls) { hall in
                                        Button(action: { selectedHallId = hall.id }) {
                                            VStack(alignment: .leading, spacing: 4) {
                                                Text(hall.name)
                                                    .font(.system(size: 13, weight: .bold))
                                                    .foregroundColor(selectedHallId == hall.id ? .black : .white)
                                                
                                                Text("\(hall.currentOccupancy) / \(hall.capacityThreshold)")
                                                    .font(.system(size: 11, design: .monospaced))
                                                    .foregroundColor(selectedHallId == hall.id ? Color.black.opacity(0.7) : MianuColors.fgMuted)
                                            }
                                            .padding(.horizontal, 14)
                                            .padding(.vertical, 10)
                                            .background(selectedHallId == hall.id ? Color.white : Color(hex: 0x14171E))
                                            .overlay(
                                                RoundedRectangle(cornerRadius: 12, style: .continuous)
                                                    .stroke(selectedHallId == hall.id ? Color.white : MianuColors.border, lineWidth: 1)
                                            )
                                            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                                        }
                                        .buttonStyle(MianuPressableButtonStyle(scale: 0.96))
                                    }
                                }
                            }
                        }
                        
                        // Occupancy Gauge
                        if let hall = activeHall {
                            VStack(spacing: 8) {
                                HStack {
                                    Text("ROOM OCCUPANCY")
                                        .font(.system(size: 11, weight: .bold, design: .monospaced))
                                        .foregroundColor(MianuColors.fgMuted)
                                    Spacer()
                                    StatusPill(
                                        text: hall.isAtCapacity ? "FULL" : "\(hall.currentOccupancy) / \(hall.capacityThreshold)",
                                        color: hall.isAtCapacity ? MianuColors.danger : (hall.isNearCapacity ? MianuColors.warning : MianuColors.success)
                                    )
                                }
                                
                                GeometryReader { geo in
                                    ZStack(alignment: .leading) {
                                        RoundedRectangle(cornerRadius: 6, style: .continuous)
                                            .fill(Color(hex: 0x1F2432))
                                            .frame(height: 10)
                                        
                                        RoundedRectangle(cornerRadius: 6, style: .continuous)
                                            .fill(hall.isAtCapacity ? MianuColors.danger : (hall.isNearCapacity ? MianuColors.warning : MianuColors.success))
                                            .frame(width: max(geo.size.width * CGFloat(hall.occupancyRatio), 8), height: 10)
                                    }
                                }
                                .frame(height: 10)
                            }
                            .padding(16)
                            .glassCard()
                        }
                        
                        // Scanner Card
                        VStack(spacing: 16) {
                            Picker("Action", selection: $selectedAction) {
                                Text("Entry Gate").tag(AccessAction.entry)
                                Text("Exit Gate").tag(AccessAction.exit)
                            }
                            .pickerStyle(SegmentedPickerStyle())
                            
                            MianuButton(
                                nfcReader.isScanning ? "Scanning Badge..." : "Tap NFC Badge to Scan Gate",
                                icon: "sensor.tag.radiowaves.forward.fill",
                                isLoading: nfcReader.isScanning,
                                isEnabled: !selectedHallId.isEmpty && !isSubmitting
                            ) {
                                startNFCScan()
                            }
                            
                            HStack {
                                Rectangle().fill(MianuColors.border).frame(height: 1)
                                Text("OR ENTER UID")
                                    .font(.system(size: 10, weight: .bold, design: .monospaced))
                                    .foregroundColor(MianuColors.fgSubtle)
                                Rectangle().fill(MianuColors.border).frame(height: 1)
                            }
                            
                            HStack(spacing: 10) {
                                MianuTextField("Card UID", placeholder: "04A2B3C4D5", leadingIcon: "creditcard.fill", text: $manualCardUid)
                                Button(action: { submitAccessScan(cardUid: manualCardUid) }) {
                                    Text("Log")
                                        .font(.system(size: 13, weight: .bold))
                                        .foregroundColor(.black)
                                        .frame(height: 48)
                                        .padding(.horizontal, 16)
                                        .background(Color.white)
                                        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                                }
                                .buttonStyle(MianuPressableButtonStyle(scale: 0.95))
                                .padding(.top, 22)
                                .disabled(manualCardUid.isEmpty || selectedHallId.isEmpty || isSubmitting)
                            }
                        }
                        .padding(20)
                        .glassPanel()
                        
                        // Access Result Banner
                        if let log = lastLog {
                            VStack(alignment: .leading, spacing: 6) {
                                HStack {
                                    Image(systemName: log.allowed ? "checkmark.circle.fill" : "xmark.circle.fill")
                                        .foregroundColor(log.allowed ? MianuColors.success : MianuColors.danger)
                                    Text(log.allowed ? "ACCESS GRANTED" : "ACCESS DENIED")
                                        .font(.system(size: 12, weight: .bold, design: .monospaced))
                                        .foregroundColor(log.allowed ? MianuColors.success : MianuColors.danger)
                                    Spacer()
                                    StatusPill(text: log.action.rawValue.uppercased(), color: log.allowed ? MianuColors.success : MianuColors.danger)
                                }
                                
                                if let reason = log.reason {
                                    Text(reason)
                                        .font(.system(size: 13))
                                        .foregroundColor(MianuColors.fgMuted)
                                }
                                
                                Text(String(log.timestamp.prefix(19)).replacingOccurrences(of: "T", with: " "))
                                    .font(.system(size: 11))
                                    .foregroundColor(MianuColors.fgSubtle)
                            }
                            .padding(16)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background((log.allowed ? MianuColors.success : MianuColors.danger).opacity(0.12))
                            .overlay(
                                RoundedRectangle(cornerRadius: 14, style: .continuous)
                                    .stroke((log.allowed ? MianuColors.success : MianuColors.danger).opacity(0.5), lineWidth: 1)
                            )
                            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                        }
                        
                        if let error = errorMessage {
                            HStack {
                                Image(systemName: "exclamationmark.triangle.fill")
                                    .foregroundColor(MianuColors.danger)
                                Text(error)
                                    .font(.system(size: 13))
                                    .foregroundColor(MianuColors.danger)
                            }
                            .padding(14)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(MianuColors.danger.opacity(0.1))
                            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                        }
                    }
                    .padding(20)
                }
                .refreshable {
                    await loadHallsAsync()
                }
                .scrollDismissesKeyboard(.interactively)
            }
            .navigationBarHidden(true)
            .onAppear { loadHalls() }
        }
        .navigationViewStyle(StackNavigationViewStyle())
    }
    
    private func loadHalls() {
        Task {
            await loadHallsAsync()
        }
    }

    private func loadHallsAsync() async {
        guard let token = session.token else { return }
        do {
            let res = try await APIService.shared.getHalls(token: token)
            await MainActor.run {
                self.halls = res
                if self.selectedHallId.isEmpty, let first = res.first {
                    self.selectedHallId = first.id
                }
            }
        } catch {
            print("Failed to load halls: \(error)")
        }
    }
    
    private func startNFCScan() {
        errorMessage = nil
        nfcReader.scan(alertMessage: "Hold iPhone to delegate badge at door") { uid in
            submitAccessScan(cardUid: uid)
        }
    }
    
    private func submitAccessScan(cardUid: String) {
        guard let token = session.token, !cardUid.isEmpty, !selectedHallId.isEmpty else { return }
        isSubmitting = true
        errorMessage = nil
        
        Task {
            do {
                let req = AccessScanRequest(cardUid: cardUid.trimmingCharacters(in: .whitespacesAndNewlines), hallId: selectedHallId, action: selectedAction)
                let res = try await APIService.shared.scanAccess(request: req, token: token)
                await MainActor.run {
                    self.lastLog = res
                    self.manualCardUid = ""
                    self.isSubmitting = false
                    UINotificationFeedbackGenerator().notificationOccurred(res.allowed ? .success : .warning)
                    self.loadHalls()
                }
            } catch {
                await MainActor.run {
                    self.errorMessage = error.localizedDescription
                    self.isSubmitting = false
                    UINotificationFeedbackGenerator().notificationOccurred(.error)
                }
            }
        }
    }
}
