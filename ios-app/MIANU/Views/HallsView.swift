import SwiftUI

public struct HallsView: View {
    @ObservedObject private var session = AppSession.shared
    
    @State private var halls: [HallResponse] = []
    @State private var isLoading = false
    @State private var showingAddHall = false
    
    public init() {}
    
    public var body: some View {
        NavigationView {
            ZStack {
                MianuColors.bg.ignoresSafeArea()
                
                ScrollView {
                    VStack(spacing: 18) {
                        HStack {
                            SectionHeader(
                                title: "Venue Rooms & Halls",
                                eyebrow: "SESSION CAPACITIES",
                                subtitle: "Real-time hall occupancy & door limits"
                            )
                            Spacer()
                            if session.currentRole == .admin || session.currentRole == .chiefOrganizer {
                                Button(action: { showingAddHall = true }) {
                                    Image(systemName: "plus")
                                        .font(.system(size: 16, weight: .bold))
                                        .foregroundColor(.black)
                                        .padding(10)
                                        .background(Color.white)
                                        .clipShape(Circle())
                                }
                            }
                        }
                        
                        if isLoading && halls.isEmpty {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                .padding(40)
                        } else {
                            LazyVStack(spacing: 12) {
                                ForEach(halls) { hall in
                                    HallCard(hall: hall)
                                }
                            }
                        }
                    }
                    .padding(20)
                }
                .refreshable {
                    await loadHallsAsync()
                }
            }
            .navigationBarHidden(true)
            .sheet(isPresented: $showingAddHall) {
                AddHallSheet(onCreated: { loadHalls() })
            }
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
        isLoading = true
        do {
            let res = try await APIService.shared.getHalls(token: token)
            await MainActor.run {
                self.halls = res
                self.isLoading = false
            }
        } catch {
            await MainActor.run {
                self.isLoading = false
            }
        }
    }
}

// MARK: - Hall Card
private struct HallCard: View {
    let hall: HallResponse
    
    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Text(hall.name)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.white)
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
                        .frame(height: 8)
                    
                    RoundedRectangle(cornerRadius: 6, style: .continuous)
                        .fill(hall.isAtCapacity ? MianuColors.danger : (hall.isNearCapacity ? MianuColors.warning : MianuColors.success))
                        .frame(width: max(geo.size.width * CGFloat(hall.occupancyRatio), 8), height: 8)
                }
            }
            .frame(height: 8)
            
            HStack {
                Text(hall.isAtCapacity ? "Capacity reached" : "\(hall.capacityThreshold - hall.currentOccupancy) seats free")
                    .font(.system(size: 11))
                    .foregroundColor(MianuColors.fgMuted)
                Spacer()
                Text("Limit: \(hall.capacityThreshold)")
                    .font(.system(size: 11, design: .monospaced))
                    .foregroundColor(MianuColors.fgSubtle)
            }
        }
        .padding(16)
        .glassCard()
    }
}

// MARK: - Add Hall Sheet
private struct AddHallSheet: View {
    @Environment(\.presentationMode) var presentationMode
    @ObservedObject private var session = AppSession.shared
    
    let onCreated: () -> Void
    
    @State private var name = ""
    @State private var capacity = "50"
    @State private var isSubmitting = false
    
    var body: some View {
        NavigationView {
            ZStack {
                MianuColors.bg.ignoresSafeArea()
                
                VStack(spacing: 16) {
                    MianuTextField("Hall / Room Name", placeholder: "e.g. Plenary Hall B", text: $name)
                    MianuTextField("Max Capacity Threshold", placeholder: "e.g. 50", text: $capacity)
                    
                    MianuButton("Create Room", isLoading: isSubmitting, isEnabled: !name.isEmpty) {
                        createHall()
                    }
                    .padding(.top, 10)
                    
                    Spacer()
                }
                .padding(20)
            }
            .navigationTitle("New Conference Hall")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { presentationMode.wrappedValue.dismiss() }
                        .foregroundColor(.white)
                }
            }
        }
    }
    
    private func createHall() {
        guard let token = session.token, let cap = Int(capacity) else { return }
        isSubmitting = true
        Task {
            do {
                let hall = HallCreate(name: name, capacityThreshold: cap, allowedRoles: [.user, .organizer, .admin])
                _ = try await APIService.shared.createHall(hall: hall, token: token)
                await MainActor.run {
                    self.isSubmitting = false
                    self.onCreated()
                    self.presentationMode.wrappedValue.dismiss()
                }
            } catch {
                await MainActor.run {
                    self.isSubmitting = false
                }
            }
        }
    }
}
