import SwiftUI

public struct MealScanView: View {
    @ObservedObject private var session = AppSession.shared
    @ObservedObject private var nfcReader = NFCReader.shared
    
    @State private var selectedMealType: MealType = .lunch
    @State private var manualCardUid = ""
    @State private var isSubmitting = false
    @State private var lastResponse: MealSwipeResponse? = nil
    @State private var errorMessage: String? = nil
    
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
                                title: "Meal Scanner",
                                eyebrow: "CATERING & REPAST",
                                subtitle: "Scan delegate smartcard to redeem meal"
                            )
                            Spacer()
                        }
                        
                        // Meal Type Picker
                        VStack(alignment: .leading, spacing: 10) {
                            Text("ACTIVE MEAL WINDOW")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(MianuColors.fgMuted)
                            
                            HStack(spacing: 12) {
                                ForEach(MealType.allCases, id: \.self) { meal in
                                    Button(action: { selectedMealType = meal }) {
                                        HStack {
                                            Image(systemName: meal == .breakfast ? "cup.and.saucer.fill" : "fork.knife")
                                            Text(meal.displayName)
                                                .font(.system(size: 14, weight: .bold))
                                        }
                                        .frame(maxWidth: .infinity)
                                        .frame(height: 48)
                                        .background(selectedMealType == meal ? Color.white : Color(hex: 0x14171E))
                                        .foregroundColor(selectedMealType == meal ? .black : MianuColors.fgMuted)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 12, style: .continuous)
                                                .stroke(selectedMealType == meal ? Color.white : MianuColors.border, lineWidth: 1)
                                        )
                                        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                                    }
                                    .buttonStyle(MianuPressableButtonStyle(scale: 0.96))
                                }
                            }
                        }
                        
                        // Scanner Card
                        VStack(spacing: 16) {
                            HStack {
                                Image(systemName: "fork.knife.circle.fill")
                                    .font(.system(size: 22))
                                    .foregroundColor(MianuColors.warning)
                                Text("Catering Reader")
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(.white)
                                Spacer()
                            }
                            
                            MianuButton(
                                nfcReader.isScanning ? "Scanning Badge..." : "Tap NFC Badge to Redeem",
                                icon: "sensor.tag.radiowaves.forward.fill",
                                isLoading: nfcReader.isScanning,
                                isEnabled: !isSubmitting
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
                                Button(action: { submitMealSwipe(cardUid: manualCardUid) }) {
                                    Text("Redeem")
                                        .font(.system(size: 13, weight: .bold))
                                        .foregroundColor(.black)
                                        .frame(height: 48)
                                        .padding(.horizontal, 16)
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
                        
                        // Result Banner
                        if let res = lastResponse {
                            VStack(alignment: .leading, spacing: 6) {
                                HStack {
                                    Image(systemName: "checkmark.circle.fill")
                                        .foregroundColor(MianuColors.success)
                                    Text("MEAL REDEEMED")
                                        .font(.system(size: 12, weight: .bold, design: .monospaced))
                                        .foregroundColor(MianuColors.success)
                                    Spacer()
                                    StatusPill(text: res.mealType.rawValue, color: MianuColors.warning)
                                }
                                
                                Text(res.userName ?? "Delegate")
                                    .font(.system(size: 18, weight: .bold))
                                    .foregroundColor(.white)
                                
                                HStack {
                                    Text("Meals Remaining: \(res.mealsRemaining ?? 0)")
                                        .font(.system(size: 13, weight: .semibold))
                                        .foregroundColor(.white)
                                    Spacer()
                                    Text(String(res.swipeTimestamp.prefix(16)).replacingOccurrences(of: "T", with: " "))
                                        .font(.system(size: 11))
                                        .foregroundColor(MianuColors.fgMuted)
                                }
                            }
                            .padding(16)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(MianuColors.success.opacity(0.12))
                            .overlay(
                                RoundedRectangle(cornerRadius: 14, style: .continuous)
                                    .stroke(MianuColors.success.opacity(0.5), lineWidth: 1)
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
                .scrollDismissesKeyboard(.interactively)
            }
            .navigationBarHidden(true)
        }
        .navigationViewStyle(StackNavigationViewStyle())
    }
    
    private func startNFCScan() {
        errorMessage = nil
        nfcReader.scan(alertMessage: "Hold iPhone to delegate meal badge") { uid in
            submitMealSwipe(cardUid: uid)
        }
    }
    
    private func submitMealSwipe(cardUid: String) {
        guard let token = session.token, !cardUid.isEmpty else { return }
        isSubmitting = true
        errorMessage = nil
        
        Task {
            do {
                let req = MealSwipeRequest(cardUid: cardUid.trimmingCharacters(in: .whitespacesAndNewlines), mealType: selectedMealType)
                let res = try await APIService.shared.swipeMeal(request: req, token: token)
                await MainActor.run {
                    self.lastResponse = res
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
