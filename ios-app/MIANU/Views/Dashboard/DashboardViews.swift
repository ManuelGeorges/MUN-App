import SwiftUI

// MARK: - Admin Dashboard
public struct AdminDashboardView: View {
    @ObservedObject private var session = AppSession.shared
    
    @State private var overview: DashboardOverview? = nil
    @State private var recentScans: [LiveScanEvent] = []
    @State private var isLoading = false
    
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
                                title: "Command Center",
                                eyebrow: "SEC-GEN CONTROL",
                                subtitle: "Live telemetry & conference operations"
                            )
                            Spacer()
                            Button(action: { loadDashboard() }) {
                                Image(systemName: "arrow.clockwise")
                                    .font(.system(size: 16))
                                    .foregroundColor(.white)
                                    .padding(10)
                                    .background(Color(hex: 0x14171E))
                                    .clipShape(Circle())
                            }
                        }
                        
                        // KPI Cards
                        if let o = overview {
                            HStack(spacing: 12) {
                                StatCard(
                                    title: "Active Delegates",
                                    value: "\(o.activeUsers)",
                                    caption: "Registered roster",
                                    icon: "person.2.fill",
                                    accentColor: MianuColors.info
                                )
                                StatCard(
                                    title: "Inside Halls",
                                    value: "\(o.peopleInsideHalls)",
                                    caption: "In session rooms",
                                    icon: "building.2.crop.circle",
                                    accentColor: MianuColors.success
                                )
                            }
                            
                            HStack(spacing: 12) {
                                StatCard(
                                    title: "Meals Served",
                                    value: "\(o.totalMealsToday)",
                                    caption: "Catering redemptions",
                                    icon: "fork.knife",
                                    accentColor: MianuColors.warning
                                )
                                StatCard(
                                    title: "Gate Scans",
                                    value: "\(o.totalAccessScansToday)",
                                    caption: "Hall door movements",
                                    icon: "door.left.hand.open"
                                )
                            }
                        }
                        
                        // Quick Action Launchers
                        VStack(alignment: .leading, spacing: 10) {
                            Text("QUICK ACTION LAUNCHERS")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(MianuColors.fgMuted)
                            
                            VStack(spacing: 10) {
                                NavigationLink(destination: AttendanceScanView()) {
                                    QuickActionRow(
                                        title: "Entrance Turnstile Scanner",
                                        subtitle: "Tap badges to record conference attendance in sum",
                                        icon: "sensor.tag.radiowaves.forward.fill",
                                        accent: MianuColors.success
                                    )
                                }
                                
                                NavigationLink(destination: LocationsView()) {
                                    QuickActionRow(
                                        title: "Venue Locations & Attendance",
                                        subtitle: "Real-time presence, single & bulk attendance overrides",
                                        icon: "mappin.and.ellipse",
                                        accent: MianuColors.info
                                    )
                                }
                                
                                NavigationLink(destination: MealScanView()) {
                                    QuickActionRow(
                                        title: "Catering & Meal Turnstile",
                                        subtitle: "Scan breakfast & lunch delegate meal allowances",
                                        icon: "fork.knife",
                                        accent: MianuColors.warning
                                    )
                                }
                                
                                NavigationLink(destination: AccessScanView()) {
                                    QuickActionRow(
                                        title: "Door Access Gate Scanner",
                                        subtitle: "Session room entry & exit clearance control",
                                        icon: "door.left.hand.open",
                                        accent: Color.white
                                    )
                                }
                            }
                        }
                        
                        // Recent Scans Feed
                        if !recentScans.isEmpty {
                            VStack(alignment: .leading, spacing: 10) {
                                Text("LIVE AUDIT SCANS")
                                    .font(.system(size: 11, weight: .bold, design: .monospaced))
                                    .foregroundColor(MianuColors.fgMuted)
                                
                                VStack(spacing: 8) {
                                    ForEach(recentScans.prefix(8)) { event in
                                        HStack {
                                            Circle()
                                                .fill(event.type == "attendance" ? MianuColors.success : (event.type == "meal" ? MianuColors.warning : MianuColors.info))
                                                .frame(width: 8, height: 8)
                                            
                                            VStack(alignment: .leading, spacing: 2) {
                                                Text(event.userName ?? "Delegate")
                                                    .font(.system(size: 13, weight: .semibold))
                                                    .foregroundColor(.white)
                                                Text(event.detail ?? event.type.uppercased())
                                                    .font(.system(size: 11))
                                                    .foregroundColor(MianuColors.fgMuted)
                                            }
                                            
                                            Spacer()
                                            
                                            Text(String(event.timestamp.prefix(19)).replacingOccurrences(of: "T", with: " "))
                                                .font(.system(size: 10, design: .monospaced))
                                                .foregroundColor(MianuColors.fgSubtle)
                                        }
                                        .padding(12)
                                        .glassCard()
                                    }
                                }
                            }
                        }
                    }
                    .padding(20)
                }
                .refreshable {
                    await loadDashboardAsync()
                }
            }
            .navigationBarHidden(true)
            .onAppear { loadDashboard() }
        }
        .navigationViewStyle(StackNavigationViewStyle())
    }
    
    private func loadDashboard() {
        Task {
            await loadDashboardAsync()
        }
    }

    private func loadDashboardAsync() async {
        guard let token = session.token else { return }
        do {
            async let ov = APIService.shared.getDashboardOverview(token: token)
            async let sc = APIService.shared.getLiveScans(token: token)
            let (resOv, resSc) = try await (ov, sc)
            await MainActor.run {
                self.overview = resOv
                self.recentScans = resSc
            }
        } catch {
            print("Dashboard error: \(error)")
        }
    }
}

// MARK: - Organizer Dashboard
public struct OrganizerDashboardView: View {
    @ObservedObject private var session = AppSession.shared
    
    public init() {}
    
    public var body: some View {
        NavigationView {
            ZStack {
                MianuColors.bg.ignoresSafeArea()
                
                ScrollView {
                    VStack(spacing: 20) {
                        SectionHeader(
                            title: "Operations Floor",
                            eyebrow: "ORGANIZER TERMINALS",
                            subtitle: "Hardware scanner quick actions"
                        )
                        .frame(maxWidth: .infinity, alignment: .leading)
                        
                        VStack(spacing: 14) {
                            NavigationLink(destination: AttendanceScanView()) {
                                QuickActionRow(
                                    title: "Attendance Turnstile",
                                    subtitle: "Register conference check-ins at entrance gate",
                                    icon: "sensor.tag.radiowaves.forward.fill",
                                    accent: MianuColors.success
                                )
                            }
                            
                            NavigationLink(destination: MealScanView()) {
                                QuickActionRow(
                                    title: "Meal Validation Terminal",
                                    subtitle: "Scan badges for breakfast and lunch service",
                                    icon: "fork.knife",
                                    accent: MianuColors.warning
                                )
                            }
                            
                            NavigationLink(destination: AccessScanView()) {
                                QuickActionRow(
                                    title: "Hall Door Access",
                                    subtitle: "Manage room capacity and entry permissions",
                                    icon: "door.left.hand.open",
                                    accent: MianuColors.info
                                )
                            }
                        }
                    }
                    .padding(20)
                }
            }
            .navigationBarHidden(true)
        }
        .navigationViewStyle(StackNavigationViewStyle())
    }
}

// MARK: - Delegate Dashboard
public struct DelegateDashboardView: View {
    @ObservedObject private var session = AppSession.shared
    
    public init() {}
    
    public var body: some View {
        NavigationView {
            ZStack {
                MianuColors.bg.ignoresSafeArea()
                
                ScrollView {
                    VStack(spacing: 20) {
                        SectionHeader(
                            title: session.currentUserName ?? "Delegate Dashboard",
                            eyebrow: "MIANU CREDENTIALS",
                            subtitle: "Your conference pass, meal status & details"
                        )
                        .frame(maxWidth: .infinity, alignment: .leading)
                        
                        // Badge Card
                        VStack(spacing: 16) {
                            HStack {
                                Image(systemName: "person.text.rectangle.fill")
                                    .font(.system(size: 24))
                                    .foregroundColor(.white)
                                Spacer()
                                StatusPill(text: session.currentRole.displayName, color: MianuColors.success)
                            }
                            
                            VStack(alignment: .leading, spacing: 4) {
                                Text(session.currentUserName ?? "Attendee")
                                    .font(.system(size: 22, weight: .bold))
                                    .foregroundColor(.white)
                                
                                if let user = session.currentUser, let uid = user.cardUid {
                                    Text("CARD UID: \(uid)")
                                        .font(.system(size: 12, design: .monospaced))
                                        .foregroundColor(MianuColors.fgMuted)
                                }
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                            
                            Divider().background(MianuColors.border)
                            
                            HStack {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text("MEALS REMAINING")
                                        .font(.system(size: 10, weight: .bold, design: .monospaced))
                                        .foregroundColor(MianuColors.fgSubtle)
                                    Text("\(session.currentUser?.mealsBalance ?? 6)")
                                        .font(.system(size: 20, weight: .bold))
                                        .foregroundColor(.white)
                                }
                                Spacer()
                                VStack(alignment: .trailing, spacing: 2) {
                                    Text("TOTAL ALLOWANCE")
                                        .font(.system(size: 10, weight: .bold, design: .monospaced))
                                        .foregroundColor(MianuColors.fgSubtle)
                                    Text("\(session.currentUser?.mealAllowance ?? 6)")
                                        .font(.system(size: 20, weight: .bold))
                                        .foregroundColor(MianuColors.fgMuted)
                                }
                            }
                        }
                        .padding(20)
                        .glassPanel()
                    }
                    .padding(20)
                }
            }
            .navigationBarHidden(true)
        }
        .navigationViewStyle(StackNavigationViewStyle())
    }
}

// MARK: - Quick Action Row Component
private struct QuickActionRow: View {
    let title: String
    let subtitle: String
    let icon: String
    var accent: Color = .white
    
    var body: some View {
        HStack(spacing: 14) {
            Image(systemName: icon)
                .font(.system(size: 20))
                .foregroundColor(accent)
                .frame(width: 44, height: 44)
                .background(accent.opacity(0.12))
                .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
            
            VStack(alignment: .leading, spacing: 3) {
                Text(title)
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(.white)
                Text(subtitle)
                    .font(.system(size: 12))
                    .foregroundColor(MianuColors.fgMuted)
            }
            
            Spacer()
            
            Image(systemName: "chevron.right")
                .font(.system(size: 12, weight: .semibold))
                .foregroundColor(MianuColors.fgSubtle)
        }
        .padding(14)
        .glassCard()
    }
}
