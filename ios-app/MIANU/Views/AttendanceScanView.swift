import SwiftUI

public struct AttendanceScanView: View {
    @ObservedObject private var session = AppSession.shared
    @ObservedObject private var nfcReader = NFCReader.shared
    
    @State private var manualCardUid = ""
    @State private var selectedAction = "check_in" // "check_in", "check_out", "toggle"
    @State private var isSubmitting = false
    @State private var summary: AttendanceSummary? = nil
    @State private var lastSwipeUser: AttendanceSwipeUser? = nil
    @State private var errorMessage: String? = nil
    @State private var isSuccessBanner = false
    
    public init() {}
    
    public var body: some View {
        NavigationView {
            ZStack {
                MianuColors.bg.ignoresSafeArea()
                
                ScrollView {
                    VStack(spacing: 20) {
                        // Header
                        HStack {
                            SectionHeader(
                                title: "Turnstile Scanner",
                                eyebrow: "CONFERENCE ATTENDANCE",
                                subtitle: "Scan delegate badge at main turnstile gate"
                            )
                            Spacer()
                            Button(action: { fetchSummary() }) {
                                Image(systemName: "arrow.clockwise")
                                    .font(.system(size: 16))
                                    .foregroundColor(.white)
                                    .padding(10)
                                    .background(Color(hex: 0x14171E))
                                    .clipShape(Circle())
                            }
                        }
                        
                        // Telemetry Cards
                        if let s = summary {
                            HStack(spacing: 12) {
                                StatCard(
                                    title: "Present in Sum",
                                    value: "\(s.totalPresent) / \(s.totalRegistered)",
                                    caption: "Attendance: \(s.attendanceRate)%",
                                    icon: "person.crop.circle.badge.checkmark",
                                    accentColor: MianuColors.success
                                )
                                
                                StatCard(
                                    title: "Awaiting Arrival",
                                    value: "\(s.totalAbsent)",
                                    caption: "Absent delegates",
                                    icon: "person.3.fill",
                                    accentColor: MianuColors.danger
                                )
                            }
                            
                            HStack(spacing: 12) {
                                StatCard(
                                    title: "Turnstile Swipes",
                                    value: "\(s.totalSwipes)",
                                    caption: "Badge taps recorded",
                                    icon: "creditcard.fill"
                                )
                                
                                StatCard(
                                    title: "Arrival Rate",
                                    value: "\(s.attendanceRate)%",
                                    caption: "Delegates checked in",
                                    icon: "chart.line.uptrend.xyaxis",
                                    accentColor: MianuColors.warning
                                )
                            }
                        }
                        
                        // Scanner Card
                        VStack(spacing: 16) {
                            HStack {
                                Image(systemName: "wave.3.forward.circle.fill")
                                    .font(.system(size: 20))
                                    .foregroundColor(MianuColors.success)
                                Text("NFC Turnstile Reader")
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(.white)
                                Spacer()
                            }
                            
                            // Mode Segmented Control
                            Picker("Action", selection: $selectedAction) {
                                Text("Check In").tag("check_in")
                                Text("Check Out").tag("check_out")
                                Text("Toggle").tag("toggle")
                            }
                            .pickerStyle(SegmentedPickerStyle())
                            
                            // Tap to Scan NFC Button
                            MianuButton(
                                nfcReader.isScanning ? "Scanning NFC..." : "Tap NFC Badge to Scan",
                                icon: "sensor.tag.radiowaves.forward.fill",
                                isLoading: nfcReader.isScanning,
                                isEnabled: !isSubmitting,
                                containerColor: .white,
                                contentColor: .black
                            ) {
                                startNFCScan()
                            }
                            
                            // Divider with "OR"
                            HStack {
                                Rectangle().fill(MianuColors.border).frame(height: 1)
                                Text("OR MANUAL UID")
                                    .font(.system(size: 10, weight: .bold, design: .monospaced))
                                    .foregroundColor(MianuColors.fgSubtle)
                                Rectangle().fill(MianuColors.border).frame(height: 1)
                            }
                            
                            // Manual UID input
                            HStack(spacing: 10) {
                                MianuTextField(
                                    "Badge UID",
                                    placeholder: "e.g. 04A2B3C4D5",
                                    leadingIcon: "creditcard.fill",
                                    text: $manualCardUid
                                )
                                
                                Button(action: { submitSwipe(cardUid: manualCardUid) }) {
                                    Text("Submit")
                                        .font(.system(size: 13, weight: .bold))
                                        .foregroundColor(.black)
                                        .padding(.horizontal, 16)
                                        .frame(height: 48)
                                        .background(Color.white)
                                        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                                }
                                .buttonStyle(MianuPressableButtonStyle(scale: 0.95))
                                .padding(.top, 22)
                                .disabled(manualCardUid.isEmpty || isSubmitting)
                            }
                        }
                        .padding(20)
                        .glassPanel()
                        
                        // Swipe Result Banner
                        if let user = lastSwipeUser {
                            VStack(alignment: .leading, spacing: 6) {
                                HStack {
                                    Image(systemName: isSuccessBanner ? "checkmark.circle.fill" : "xmark.circle.fill")
                                        .foregroundColor(isSuccessBanner ? MianuColors.success : MianuColors.danger)
                                    Text(isSuccessBanner ? "ATTENDANCE REGISTERED" : "CHECK-OUT RECORDED")
                                        .font(.system(size: 12, weight: .bold, design: .monospaced))
                                        .foregroundColor(isSuccessBanner ? MianuColors.success : MianuColors.danger)
                                    Spacer()
                                    StatusPill(text: user.status, color: isSuccessBanner ? MianuColors.success : MianuColors.danger)
                                }
                                
                                Text(user.name)
                                    .font(.system(size: 18, weight: .bold))
                                    .foregroundColor(.white)
                                
                                Text("\(user.role.capitalized) • \(user.teamName ?? "General") • Card: \(user.cardUid)")
                                    .font(.system(size: 12))
                                    .foregroundColor(MianuColors.fgMuted)
                            }
                            .padding(16)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background((isSuccessBanner ? MianuColors.success : MianuColors.danger).opacity(0.12))
                            .overlay(
                                RoundedRectangle(cornerRadius: 14, style: .continuous)
                                    .stroke((isSuccessBanner ? MianuColors.success : MianuColors.danger).opacity(0.5), lineWidth: 1)
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
                    await fetchSummaryAsync()
                }
                .scrollDismissesKeyboard(.interactively)
            }
            .navigationBarHidden(true)
            .onAppear {
                fetchSummary()
            }
        }
        .navigationViewStyle(StackNavigationViewStyle())
    }
    
    private func fetchSummary() {
        Task {
            await fetchSummaryAsync()
        }
    }

    private func fetchSummaryAsync() async {
        guard let token = session.token else { return }
        do {
            let res = try await APIService.shared.getAttendance(token: token)
            await MainActor.run {
                self.summary = res.summary
            }
        } catch {
            print("Failed to fetch attendance summary: \(error)")
        }
    }
    
    private func startNFCScan() {
        errorMessage = nil
        nfcReader.scan(alertMessage: "Hold iPhone to delegate badge turnstile") { scannedUID in
            submitSwipe(cardUid: scannedUID)
        }
    }
    
    private func submitSwipe(cardUid: String) {
        guard let token = session.token, !cardUid.isEmpty else { return }
        isSubmitting = true
        errorMessage = nil
        
        Task {
            do {
                let req = AttendanceSwipeRequest(cardUid: cardUid.trimmingCharacters(in: .whitespacesAndNewlines), action: selectedAction)
                let res = try await APIService.shared.recordAttendanceSwipe(request: req, token: token)
                await MainActor.run {
                    self.lastSwipeUser = res.user
                    self.summary = res.summary
                    self.isSuccessBanner = (res.user.status == "present")
                    self.manualCardUid = ""
                    self.isSubmitting = false
                    UINotificationFeedbackGenerator().notificationOccurred(.success)
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
