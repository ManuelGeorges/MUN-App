import SwiftUI

public struct MainTabView: View {
    @ObservedObject private var session = AppSession.shared
    @State private var selectedTab = 0
    
    public init() {
        // Configure dark translucent TabBar appearance
        let appearance = UITabBarAppearance()
        appearance.configureWithOpaqueBackground()
        appearance.backgroundColor = UIColor(Color(hex: 0x0A0D12))
        appearance.shadowColor = UIColor(Color(hex: 0x1E2433))
        
        let itemAppearance = UITabBarItemAppearance()
        itemAppearance.normal.iconColor = UIColor(Color(hex: 0x64748B))
        itemAppearance.normal.titleTextAttributes = [.foregroundColor: UIColor(Color(hex: 0x64748B))]
        itemAppearance.selected.iconColor = UIColor.white
        itemAppearance.selected.titleTextAttributes = [.foregroundColor: UIColor.white]
        
        appearance.stackedLayoutAppearance = itemAppearance
        appearance.inlineLayoutAppearance = itemAppearance
        appearance.compactInlineLayoutAppearance = itemAppearance
        
        UITabBar.appearance().standardAppearance = appearance
        if #available(iOS 15.0, *) {
            UITabBar.appearance().scrollEdgeAppearance = appearance
        }
    }
    
    public var body: some View {
        TabView(selection: $selectedTab) {
            switch session.currentRole {
            case .admin, .chiefOrganizer:
                AdminDashboardView()
                    .tabItem {
                        Image(systemName: "square.grid.2x2.fill")
                        Text("Dashboard")
                    }
                    .tag(0)
                
                UsersView()
                    .tabItem {
                        Image(systemName: "person.2.fill")
                        Text("Delegates")
                    }
                    .tag(1)
                
                LocationsView()
                    .tabItem {
                        Image(systemName: "mappin.and.ellipse")
                        Text("Locations")
                    }
                    .tag(2)
                
                BroadcastView()
                    .tabItem {
                        Image(systemName: "megaphone.fill")
                        Text("Broadcast")
                    }
                    .tag(3)
                
                SettingsView()
                    .tabItem {
                        Image(systemName: "gearshape.fill")
                        Text("Settings")
                    }
                    .tag(4)
                
            case .organizer:
                AttendanceScanView()
                    .tabItem {
                        Image(systemName: "person.crop.circle.badge.checkmark")
                        Text("Attendance")
                    }
                    .tag(0)
                
                MealScanView()
                    .tabItem {
                        Image(systemName: "fork.knife")
                        Text("Meals")
                    }
                    .tag(1)
                
                AccessScanView()
                    .tabItem {
                        Image(systemName: "door.left.hand.open")
                        Text("Access")
                    }
                    .tag(2)
                
                SettingsView()
                    .tabItem {
                        Image(systemName: "gearshape.fill")
                        Text("Settings")
                    }
                    .tag(3)
                
            case .teamLeader:
                AdminDashboardView()
                    .tabItem {
                        Image(systemName: "square.grid.2x2.fill")
                        Text("Dashboard")
                    }
                    .tag(0)
                
                BroadcastView()
                    .tabItem {
                        Image(systemName: "megaphone.fill")
                        Text("Broadcast")
                    }
                    .tag(1)
                
                TeamsView()
                    .tabItem {
                        Image(systemName: "person.3.fill")
                        Text("Team")
                    }
                    .tag(2)
                
                AvailabilityView()
                    .tabItem {
                        Image(systemName: "chair.lounge.fill")
                        Text("Availability")
                    }
                    .tag(3)
                
                SettingsView()
                    .tabItem {
                        Image(systemName: "gearshape.fill")
                        Text("Settings")
                    }
                    .tag(4)
                
            case .teamMember, .user:
                DelegateDashboardView()
                    .tabItem {
                        Image(systemName: "creditcard.fill")
                        Text("Badge")
                    }
                    .tag(0)
                
                AvailabilityView()
                    .tabItem {
                        Image(systemName: "chair.lounge.fill")
                        Text("Availability")
                    }
                    .tag(1)
                
                SettingsView()
                    .tabItem {
                        Image(systemName: "gearshape.fill")
                        Text("Settings")
                    }
                    .tag(2)
            }
        }
        .accentColor(.white)
    }
}
