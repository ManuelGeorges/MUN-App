import SwiftUI

public struct ReportsView: View {
    @ObservedObject private var session = AppSession.shared
    
    @State private var mealsReport: ReportSummary? = nil
    @State private var usersReport: ReportSummary? = nil
    @State private var dailyAnalytics: AnalyticsSummary? = nil
    @State private var isLoading = false
    @State private var exportStatus: String? = nil
    
    public init() {}
    
    public var body: some View {
        NavigationView {
            ZStack {
                MianuColors.bg.ignoresSafeArea()
                
                ScrollView {
                    VStack(spacing: 20) {
                        HStack {
                            SectionHeader(
                                title: "Operations Audit",
                                eyebrow: "REPORTS & TELEMETRY",
                                subtitle: "Conference analytics & exportable logs"
                            )
                            Spacer()
                            Button(action: { loadReports() }) {
                                Image(systemName: "arrow.clockwise")
                                    .font(.system(size: 16))
                                    .foregroundColor(.white)
                                    .padding(10)
                                    .background(Color(hex: 0x14171E))
                                    .clipShape(Circle())
                            }
                        }
                        
                        // Summary Cards
                        HStack(spacing: 12) {
                            StatCard(
                                title: "Meals Logged",
                                value: "\(mealsReport?.totalRecords ?? 0)",
                                caption: "Total redemptions",
                                icon: "fork.knife",
                                accentColor: MianuColors.warning
                            )
                            
                            StatCard(
                                title: "Active Roster",
                                value: "\(usersReport?.totalRecords ?? 0)",
                                caption: "Total accounts",
                                icon: "person.2.fill",
                                accentColor: MianuColors.info
                            )
                        }
                        
                        // Export Card
                        VStack(alignment: .leading, spacing: 12) {
                            Text("AUDIT LOG EXPORT")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(MianuColors.fgMuted)
                            
                            Text("Export all attendance swipes, room access logs, and catering transactions as standardized CSV / JSON.")
                                .font(.system(size: 13))
                                .foregroundColor(MianuColors.fgMuted)
                            
                            if let status = exportStatus {
                                Text(status)
                                    .font(.system(size: 12))
                                    .foregroundColor(MianuColors.success)
                            }
                            
                            MianuButton("Generate & Export Full Log", icon: "square.and.arrow.up.fill") {
                                performExport()
                            }
                        }
                        .padding(18)
                        .glassPanel()
                    }
                    .padding(20)
                }
            }
            .navigationBarHidden(true)
            .onAppear { loadReports() }
        }
        .navigationViewStyle(StackNavigationViewStyle())
    }
    
    private func loadReports() {
        guard let token = session.token else { return }
        isLoading = true
        Task {
            do {
                async let m = APIService.shared.getMealsReport(token: token)
                async let u = APIService.shared.getUsersReport(token: token)
                let (resM, resU) = try await (m, u)
                await MainActor.run {
                    self.mealsReport = resM
                    self.usersReport = resU
                    self.isLoading = false
                }
            } catch {
                await MainActor.run {
                    self.isLoading = false
                }
            }
        }
    }
    
    private func performExport() {
        guard let token = session.token else { return }
        Task {
            do {
                let res = try await APIService.shared.exportReport(token: token)
                await MainActor.run {
                    self.exportStatus = res["message"] ?? "Export generated successfully."
                }
            } catch {
                await MainActor.run {
                    self.exportStatus = "Export failed: \(error.localizedDescription)"
                }
            }
        }
    }
}
