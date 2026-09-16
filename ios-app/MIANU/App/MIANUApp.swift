import SwiftUI

@main
struct MIANUApp: App {
    @StateObject private var session = AppSession.shared
    
    init() {
        // Global dark status bar & appearance setup
        UINavigationBar.appearance().barTintColor = UIColor(Color(hex: 0x08090C))
        UINavigationBar.appearance().titleTextAttributes = [.foregroundColor: UIColor.white]
        UINavigationBar.appearance().largeTitleTextAttributes = [.foregroundColor: UIColor.white]
    }
    
    var body: some Scene {
        WindowGroup {
            ZStack {
                if session.isAuthenticated {
                    MainTabView()
                        .transition(.opacity)
                } else {
                    LoginView()
                        .transition(.opacity)
                }
            }
            .preferredColorScheme(.dark)
            .animation(.easeInOut(duration: 0.25), value: session.isAuthenticated)
        }
    }
}
