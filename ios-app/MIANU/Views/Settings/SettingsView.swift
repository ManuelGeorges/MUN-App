import SwiftUI

public struct SettingsView: View {
    @ObservedObject private var session = AppSession.shared
    @ObservedObject private var config = Config.shared
    
    @State private var customBaseURL = ""
    @State private var showingSavedAlert = false
    
    // Meal windows
    @State private var bStart = "07:00"
    @State private var bEnd = "10:00"
    @State private var lStart = "12:00"
    @State private var lEnd = "15:00"
    @State private var isSavingMealWindows = false
    
    public init() {}
    
    public var body: some View {
        NavigationView {
            ZStack {
                MianuColors.bg.ignoresSafeArea()
                
                ScrollView {
                    VStack(spacing: 20) {
                        HStack {
                            SectionHeader(
                                title: "Settings",
                                eyebrow: "SYSTEM CONFIG",
                                subtitle: "Profile, endpoints & operational schedules"
                            )
                            Spacer()
                        }
                        
                        // Profile Info
                        VStack(spacing: 14) {
                            HStack(spacing: 14) {
                                AvatarView(
                                    initials: session.currentUser?.initials ?? "AD",
                                    size: 54
                                )
                                
                                VStack(alignment: .leading, spacing: 3) {
                                    Text(session.currentUser?.name ?? session.currentUserName ?? "Administrator")
                                        .font(.system(size: 17, weight: .bold))
                                        .foregroundColor(.white)
                                    Text(session.currentUser?.email ?? "admin@mianu.org")
                                        .font(.system(size: 13))
                                        .foregroundColor(MianuColors.fgMuted)
                                    StatusPill(text: session.currentRole.displayName, color: MianuColors.info)
                                        .padding(.top, 2)
                                }
                                Spacer()
                            }
                        }
                        .padding(18)
                        .glassCard()
                        
                        // API Endpoint Configuration
                        VStack(alignment: .leading, spacing: 14) {
                            Text("BACKEND API ENDPOINT")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(MianuColors.fgMuted)
                            
                            MianuTextField("Base URL", placeholder: Config.defaultBaseURL, text: $customBaseURL)
                            
                            HStack(spacing: 10) {
                                Button("Reset Default") {
                                    config.resetToDefault()
                                    customBaseURL = config.baseURL
                                }
                                .font(.system(size: 12, weight: .semibold))
                                .foregroundColor(MianuColors.fgMuted)
                                
                                Spacer()
                                
                                MianuButton("Save Endpoint", isEnabled: !customBaseURL.isEmpty) {
                                    config.baseURL = customBaseURL.trimmingCharacters(in: .whitespacesAndNewlines)
                                    showingSavedAlert = true
                                }
                                .frame(width: 150)
                            }
                        }
                        .padding(18)
                        .glassPanel()
                        
                        // Meal Windows Config (Admin only)
                        if session.currentRole == .admin || session.currentRole == .chiefOrganizer {
                            VStack(alignment: .leading, spacing: 14) {
                                Text("CATERING SCHEDULES")
                                    .font(.system(size: 11, weight: .bold, design: .monospaced))
                                    .foregroundColor(MianuColors.fgMuted)
                                
                                HStack(spacing: 10) {
                                    MianuTextField("Breakfast Start", text: $bStart)
                                    MianuTextField("Breakfast End", text: $bEnd)
                                }
                                
                                HStack(spacing: 10) {
                                    MianuTextField("Lunch Start", text: $lStart)
                                    MianuTextField("Lunch End", text: $lEnd)
                                }
                                
                                MianuButton("Update Meal Windows", isLoading: isSavingMealWindows) {
                                    saveMealWindows()
                                }
                            }
                            .padding(18)
                            .glassCard()
                        }
                        
                        // TestFlight & App Info
                        VStack(alignment: .leading, spacing: 6) {
                            HStack {
                                Text("APP VERSION")
                                    .font(.system(size: 11, weight: .bold, design: .monospaced))
                                    .foregroundColor(MianuColors.fgMuted)
                                Spacer()
                                Text("1.0.0 (Build 1) TestFlight")
                                    .font(.system(size: 12, design: .monospaced))
                                    .foregroundColor(.white)
                            }
                            Divider().background(MianuColors.border)
                            HStack {
                                Text("EDGE WORKER")
                                    .font(.system(size: 11, weight: .bold, design: .monospaced))
                                    .foregroundColor(MianuColors.fgMuted)
                                Spacer()
                                Text("Cloudflare D1 SQLite")
                                    .font(.system(size: 12))
                                    .foregroundColor(MianuColors.success)
                            }
                        }
                        .padding(16)
                        .glassCard()
                        
                        // Logout Button
                        Button(action: { session.logout() }) {
                            HStack {
                                Image(systemName: "rectangle.portrait.and.arrow.right")
                                Text("Sign Out of MIANU-SM")
                            }
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(MianuColors.danger)
                            .frame(maxWidth: .infinity)
                            .frame(height: 48)
                            .background(MianuColors.danger.opacity(0.1))
                            .overlay(
                                RoundedRectangle(cornerRadius: 12, style: .continuous)
                                    .stroke(MianuColors.danger.opacity(0.3), lineWidth: 1)
                            )
                            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                        }
                        .padding(.top, 10)
                    }
                    .padding(20)
                }
            }
            .navigationBarHidden(true)
            .alert(isPresented: $showingSavedAlert) {
                Alert(title: Text("Settings Saved"), message: Text("The API endpoint has been updated to \(config.baseURL)"), dismissButton: .default(Text("OK")))
            }
            .onAppear {
                customBaseURL = config.baseURL
            }
        }
        .navigationViewStyle(StackNavigationViewStyle())
    }
    
    private func saveMealWindows() {
        guard let token = session.token else { return }
        isSavingMealWindows = true
        Task {
            do {
                let req = MealWindowRequest(breakfastStart: bStart, breakfastEnd: bEnd, lunchStart: lStart, lunchEnd: lEnd)
                _ = try await APIService.shared.updateMealWindows(request: req, token: token)
                await MainActor.run {
                    self.isSavingMealWindows = false
                    self.showingSavedAlert = true
                }
            } catch {
                await MainActor.run {
                    self.isSavingMealWindows = false
                }
            }
        }
    }
}
