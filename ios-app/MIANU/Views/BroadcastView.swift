import SwiftUI

public struct BroadcastView: View {
    @ObservedObject private var session = AppSession.shared
    
    @State private var notifications: [NotificationResponse] = []
    @State private var isLoading = false
    @State private var showingCompose = false
    
    public init() {}
    
    public var body: some View {
        NavigationView {
            ZStack {
                MianuColors.bg.ignoresSafeArea()
                
                ScrollView {
                    VStack(spacing: 18) {
                        HStack {
                            SectionHeader(
                                title: "Announcements",
                                eyebrow: "LIVE BROADCAST FEED",
                                subtitle: "Conference alerts, notices & committee updates"
                            )
                            Spacer()
                            if session.currentRole == .admin || session.currentRole == .chiefOrganizer || session.currentRole == .teamLeader {
                                Button(action: { showingCompose = true }) {
                                    Image(systemName: "square.and.pencil")
                                        .font(.system(size: 16, weight: .bold))
                                        .foregroundColor(.black)
                                        .padding(10)
                                        .background(Color.white)
                                        .clipShape(Circle())
                                }
                            }
                        }
                        
                        if isLoading && notifications.isEmpty {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                .padding(40)
                        } else if notifications.isEmpty {
                            EmptyStateView(
                                title: "No Broadcasts Yet",
                                description: "Event notifications will appear here in real time.",
                                icon: "megaphone.fill"
                            )
                        } else {
                            LazyVStack(spacing: 12) {
                                ForEach(notifications) { item in
                                    NotificationCard(item: item)
                                }
                            }
                        }
                    }
                    .padding(20)
                }
            }
            .navigationBarHidden(true)
            .sheet(isPresented: $showingCompose) {
                ComposeBroadcastSheet(onSent: { loadNotifications() })
            }
            .onAppear { loadNotifications() }
        }
        .navigationViewStyle(StackNavigationViewStyle())
    }
    
    private func loadNotifications() {
        guard let token = session.token else { return }
        isLoading = true
        Task {
            do {
                let res = try await APIService.shared.getNotifications(limit: 50, token: token)
                await MainActor.run {
                    self.notifications = res
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

// MARK: - Notification Card
private struct NotificationCard: View {
    let item: NotificationResponse
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                StatusPill(text: item.audienceLabel, color: MianuColors.info)
                Spacer()
                Text(String(item.timestamp.prefix(16)).replacingOccurrences(of: "T", with: " "))
                    .font(.system(size: 11, design: .monospaced))
                    .foregroundColor(MianuColors.fgSubtle)
            }
            
            Text(item.message)
                .font(.system(size: 14))
                .foregroundColor(.white)
            
            HStack {
                Text("Sent by: \(item.senderName ?? "MIANU Secretariat")")
                    .font(.system(size: 11))
                    .foregroundColor(MianuColors.fgMuted)
                Spacer()
            }
        }
        .padding(16)
        .glassCard()
    }
}

// MARK: - Compose Broadcast Sheet
private struct ComposeBroadcastSheet: View {
    @Environment(\.presentationMode) var presentationMode
    @ObservedObject private var session = AppSession.shared
    
    let onSent: () -> Void
    
    @State private var message = ""
    @State private var audience: BroadcastAudience = .all
    @State private var isSubmitting = false
    
    var body: some View {
        NavigationView {
            ZStack {
                MianuColors.bg.ignoresSafeArea()
                
                VStack(spacing: 18) {
                    VStack(alignment: .leading, spacing: 6) {
                        Text("TARGET AUDIENCE")
                            .font(.system(size: 12, weight: .medium))
                            .foregroundColor(MianuColors.fgMuted)
                        Picker("Audience", selection: $audience) {
                            ForEach(BroadcastAudience.allCases, id: \.self) { a in
                                Text(a.label).tag(a)
                            }
                        }
                        .pickerStyle(SegmentedPickerStyle())
                    }
                    
                    VStack(alignment: .leading, spacing: 6) {
                        Text("MESSAGE")
                            .font(.system(size: 12, weight: .medium))
                            .foregroundColor(MianuColors.fgMuted)
                        
                        TextEditor(text: $message)
                            .padding(8)
                            .frame(height: 140)
                            .background(Color(hex: 0x12151D))
                            .foregroundColor(.white)
                            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                            .overlay(
                                RoundedRectangle(cornerRadius: 12, style: .continuous)
                                    .stroke(MianuColors.border, lineWidth: 1)
                            )
                    }
                    
                    MianuButton("Send Broadcast", icon: "paperplane.fill", isLoading: isSubmitting, isEnabled: !message.isEmpty) {
                        sendBroadcast()
                    }
                    .padding(.top, 10)
                    
                    Spacer()
                }
                .padding(20)
            }
            .navigationTitle("New Broadcast")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { presentationMode.wrappedValue.dismiss() }
                        .foregroundColor(.white)
                }
            }
        }
    }
    
    private func sendBroadcast() {
        guard let token = session.token, let senderId = session.currentUserId else { return }
        isSubmitting = true
        Task {
            do {
                let req = BroadcastCreate(message: message, audience: audience)
                _ = try await APIService.shared.broadcast(senderId: senderId, body: req, token: token)
                await MainActor.run {
                    self.isSubmitting = false
                    self.onSent()
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
