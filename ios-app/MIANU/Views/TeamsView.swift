import SwiftUI

public struct TeamsView: View {
    @ObservedObject private var session = AppSession.shared
    
    @State private var teams: [TeamResponse] = []
    @State private var isLoading = false
    @State private var showingAddTeam = false
    @State private var selectedTeam: TeamResponse? = nil
    
    public init() {}
    
    public var body: some View {
        NavigationView {
            ZStack {
                MianuColors.bg.ignoresSafeArea()
                
                ScrollView {
                    VStack(spacing: 18) {
                        HStack {
                            SectionHeader(
                                title: "Committees & Teams",
                                eyebrow: "DELEGATION ROSTERS",
                                subtitle: "Manage quotas & team memberships"
                            )
                            Spacer()
                            if session.currentRole == .admin || session.currentRole == .chiefOrganizer {
                                Button(action: { showingAddTeam = true }) {
                                    Image(systemName: "plus")
                                        .font(.system(size: 16, weight: .bold))
                                        .foregroundColor(.black)
                                        .padding(10)
                                        .background(Color.white)
                                        .clipShape(Circle())
                                }
                            }
                        }
                        
                        if isLoading && teams.isEmpty {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                .padding(40)
                        } else {
                            LazyVStack(spacing: 12) {
                                ForEach(teams) { team in
                                    TeamCard(team: team)
                                }
                            }
                        }
                    }
                    .padding(20)
                }
                .refreshable {
                    await loadTeamsAsync()
                }
            }
            .navigationBarHidden(true)
            .sheet(isPresented: $showingAddTeam) {
                AddTeamSheet(onCreated: { loadTeams() })
            }
            .onAppear { loadTeams() }
        }
        .navigationViewStyle(StackNavigationViewStyle())
    }
    
    private func loadTeams() {
        Task {
            await loadTeamsAsync()
        }
    }

    private func loadTeamsAsync() async {
        guard let token = session.token else { return }
        isLoading = true
        do {
            let res = try await APIService.shared.getTeams(token: token)
            await MainActor.run {
                self.teams = res
                self.isLoading = false
            }
        } catch {
            await MainActor.run {
                self.isLoading = false
            }
        }
    }
}

// MARK: - Team Card
private struct TeamCard: View {
    let team: TeamResponse
    
    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Text(team.name)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.white)
                Spacer()
                StatusPill(
                    text: "\(team.currentSize) / \(team.capacity)",
                    color: team.isFull ? MianuColors.danger : MianuColors.success
                )
            }
            
            // Progress Bar
            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    RoundedRectangle(cornerRadius: 6, style: .continuous)
                        .fill(Color(hex: 0x1F2432))
                        .frame(height: 8)
                    
                    RoundedRectangle(cornerRadius: 6, style: .continuous)
                        .fill(team.isFull ? MianuColors.danger : MianuColors.success)
                        .frame(width: max(geo.size.width * CGFloat(team.occupancyRatio), 8), height: 8)
                }
            }
            .frame(height: 8)
            
            HStack {
                Text(team.isFull ? "At Max Quota" : "\(team.capacity - team.currentSize) spots remaining")
                    .font(.system(size: 11))
                    .foregroundColor(MianuColors.fgMuted)
                Spacer()
            }
        }
        .padding(16)
        .glassCard()
    }
}

// MARK: - Add Team Sheet
private struct AddTeamSheet: View {
    @Environment(\.presentationMode) var presentationMode
    @ObservedObject private var session = AppSession.shared
    
    let onCreated: () -> Void
    
    @State private var name = ""
    @State private var capacity = "25"
    @State private var isSubmitting = false
    
    var body: some View {
        NavigationView {
            ZStack {
                MianuColors.bg.ignoresSafeArea()
                
                VStack(spacing: 16) {
                    MianuTextField("Committee / Team Name", placeholder: "e.g. Security Council", text: $name)
                    MianuTextField("Capacity Quota", placeholder: "e.g. 25", text: $capacity)
                    
                    MianuButton("Create Delegation", isLoading: isSubmitting, isEnabled: !name.isEmpty) {
                        createTeam()
                    }
                    .padding(.top, 10)
                    
                    Spacer()
                }
                .padding(20)
            }
            .navigationTitle("New Delegation")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { presentationMode.wrappedValue.dismiss() }
                        .foregroundColor(.white)
                }
            }
        }
    }
    
    private func createTeam() {
        guard let token = session.token, let cap = Int(capacity) else { return }
        isSubmitting = true
        Task {
            do {
                let team = TeamCreate(name: name, capacity: cap)
                _ = try await APIService.shared.createTeam(team: team, token: token)
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
