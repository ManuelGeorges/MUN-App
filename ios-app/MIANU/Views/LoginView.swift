import SwiftUI

public struct LoginView: View {
    @ObservedObject private var session = AppSession.shared
    
    @State private var username = ""
    @State private var password = ""
    @State private var isLoading = false
    @State private var errorMessage: String? = nil
    
    public init() {}
    
    public var body: some View {
        ZStack {
            MianuColors.bg.ignoresSafeArea()
            
            ScrollView {
                VStack(spacing: 28) {
                    Spacer(minLength: 40)
                    
                    // Brand / Logo
                    VStack(spacing: 8) {
                        Image(systemName: "shield.checkered")
                            .font(.system(size: 52))
                            .foregroundColor(.white)
                        
                        Text("MIANU-SM")
                            .font(.system(size: 28, weight: .bold, design: .monospaced))
                            .foregroundColor(.white)
                            .tracking(2)
                        
                        Text("Session Management & Turnstile Ops")
                            .font(.system(size: 13))
                            .foregroundColor(MianuColors.fgMuted)
                    }
                    .padding(.bottom, 10)
                    
                    // Form Glass Card
                    VStack(spacing: 18) {
                        MianuTextField(
                            "Email / Username",
                            placeholder: "e.g. admin@mianu.org",
                            leadingIcon: "envelope.fill",
                            text: $username
                        )
                        
                        MianuTextField(
                            "Password",
                            placeholder: "Enter your password",
                            leadingIcon: "lock.fill",
                            text: $password,
                            isSecure: true
                        )
                        
                        if let error = errorMessage {
                            HStack(spacing: 8) {
                                Image(systemName: "exclamationmark.triangle.fill")
                                    .foregroundColor(MianuColors.danger)
                                Text(error)
                                    .font(.system(size: 12))
                                    .foregroundColor(MianuColors.danger)
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(10)
                            .background(MianuColors.danger.opacity(0.1))
                            .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
                        }
                        
                        MianuButton(
                            "Sign In",
                            icon: "arrow.right",
                            isLoading: isLoading,
                            isEnabled: !username.isEmpty && !password.isEmpty
                        ) {
                            performLogin()
                        }
                    }
                    .padding(20)
                    .glassPanel()
                    
                    // Floor Test Presets
                    VStack(spacing: 10) {
                        Text("QUICK TEST PRESETS")
                            .font(.system(size: 11, weight: .bold, design: .monospaced))
                            .foregroundColor(MianuColors.fgSubtle)
                        
                        HStack(spacing: 8) {
                            Button("Admin") {
                                username = "admin@mianu.org"
                                password = "Admin@2026"
                            }
                            .buttonStyle(PresetButtonStyle())
                            
                            Button("Organizer") {
                                username = "ops@mianu.org"
                                password = "Admin@2026"
                            }
                            .buttonStyle(PresetButtonStyle())
                            
                            Button("Delegate") {
                                username = "jean.dupont@delegate.org"
                                password = "Admin@2026"
                            }
                            .buttonStyle(PresetButtonStyle())
                        }
                    }
                    .padding(.top, 10)
                    
                    Spacer(minLength: 40)
                }
                .padding(.horizontal, 24)
            }
        }
    }
    
    private func performLogin() {
        isLoading = true
        errorMessage = nil
        
        Task {
            do {
                let req = LoginRequest(username: username.trimmingCharacters(in: .whitespacesAndNewlines), password: password)
                let tokenPair = try await APIService.shared.login(request: req)
                await MainActor.run {
                    self.isLoading = false
                    self.session.setSession(from: tokenPair)
                }
            } catch {
                await MainActor.run {
                    self.isLoading = false
                    self.errorMessage = error.localizedDescription
                }
            }
        }
    }
}

private struct PresetButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.system(size: 11, weight: .semibold))
            .foregroundColor(MianuColors.fgMuted)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(Color(hex: 0x14171E))
            .overlay(
                Capsule().stroke(MianuColors.border, lineWidth: 1)
            )
            .clipShape(Capsule())
            .opacity(configuration.isPressed ? 0.7 : 1.0)
    }
}
