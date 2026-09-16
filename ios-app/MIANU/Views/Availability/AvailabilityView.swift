import SwiftUI

public struct AvailabilityView: View {
    @ObservedObject private var session = AppSession.shared
    
    @State private var rooms: [HallAvailability] = []
    @State private var isLoading = false
    
    public init() {}
    
    public var body: some View {
        NavigationView {
            ZStack {
                MianuColors.bg.ignoresSafeArea()
                
                ScrollView {
                    VStack(spacing: 18) {
                        HStack {
                            SectionHeader(
                                title: "Room Availability",
                                eyebrow: "SEAT FINDER",
                                subtitle: "Find available seats across session halls"
                            )
                            Spacer()
                            Button(action: { loadAvailability() }) {
                                Image(systemName: "arrow.clockwise")
                                    .font(.system(size: 16))
                                    .foregroundColor(.white)
                                    .padding(10)
                                    .background(Color(hex: 0x14171E))
                                    .clipShape(Circle())
                            }
                        }
                        
                        if isLoading && rooms.isEmpty {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                .padding(40)
                        } else {
                            LazyVStack(spacing: 12) {
                                ForEach(rooms) { room in
                                    RoomAvailabilityCard(room: room)
                                }
                            }
                        }
                    }
                    .padding(20)
                }
            }
            .navigationBarHidden(true)
            .onAppear { loadAvailability() }
        }
        .navigationViewStyle(StackNavigationViewStyle())
    }
    
    private func loadAvailability() {
        guard let token = session.token else { return }
        isLoading = true
        Task {
            do {
                let res = try await APIService.shared.getHallAvailability(token: token)
                await MainActor.run {
                    self.rooms = res
                    self.isLoading = false
                }
            } catch {
                await MainActor.run {
                    self.isLoading = false
                }
            }
        }
    }
}

// MARK: - Room Card
private struct RoomAvailabilityCard: View {
    let room: HallAvailability
    
    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Text(room.name)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.white)
                Spacer()
                StatusPill(text: room.state.uppercased(), color: stateColor(room.state))
            }
            
            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    RoundedRectangle(cornerRadius: 6, style: .continuous)
                        .fill(Color(hex: 0x1F2432))
                        .frame(height: 8)
                    
                    RoundedRectangle(cornerRadius: 6, style: .continuous)
                        .fill(stateColor(room.state))
                        .frame(width: max(geo.size.width * CGFloat(room.occupancyRatio), 8), height: 8)
                }
            }
            .frame(height: 8)
            
            HStack {
                Text("\(room.seatsFree) seats available")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(.white)
                Spacer()
                Text("\(room.occupancy) / \(room.capacity) occupied")
                    .font(.system(size: 11))
                    .foregroundColor(MianuColors.fgMuted)
            }
        }
        .padding(16)
        .glassCard()
    }
    
    private func stateColor(_ state: String) -> Color {
        switch state.lowercased() {
        case "empty": return MianuColors.info
        case "available": return MianuColors.success
        case "filling": return MianuColors.warning
        default: return MianuColors.danger
        }
    }
}
