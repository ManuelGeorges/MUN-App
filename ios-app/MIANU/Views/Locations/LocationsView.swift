import SwiftUI

public struct LocationsView: View {
    @ObservedObject private var session = AppSession.shared
    @ObservedObject private var nfcReader = NFCReader.shared
    
    @State private var locations: [DelegateLocation] = []
    @State private var summary: AttendanceSummary? = nil
    @State private var isLoading = false
    @State private var isSubmitting = false
    @State private var searchQuery = ""
    @State private var selectedFilter = "all" // "all", "present", "absent", "inside", "exited"
    @State private var selectedUserIds: Set<String> = []
    
    // Turnstile panel state
    @State private var cardUidInput = ""
    @State private var swipeAction = "check_in"
    @State private var swipeFeedback: AttendanceSwipeUser? = nil
    @State private var errorMessage: String? = nil
    
    public init() {}
    
    private var filteredList: [DelegateLocation] {
        locations.filter { item in
            let matchesFilter: Bool
            switch selectedFilter {
            case "present": matchesFilter = item.isPresent
            case "absent": matchesFilter = !item.isPresent
            case "inside": matchesFilter = item.isInside
            case "exited": matchesFilter = item.isExited
            default: matchesFilter = true
            }
            
            let query = searchQuery.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
            let matchesQuery = query.isEmpty ||
                item.name.lowercased().contains(query) ||
                (item.teamName?.lowercased().contains(query) ?? false) ||
                (item.cardUid?.lowercased().contains(query) ?? false) ||
                (item.currentHallName?.lowercased().contains(query) ?? false)
            
            return matchesFilter && matchesQuery
        }
    }
    
    private var allFilteredSelected: Bool {
        !filteredList.isEmpty && filteredList.allSatisfy { selectedUserIds.contains($0.userId) }
    }
    
    public var body: some View {
        NavigationView {
            ZStack {
                MianuColors.bg.ignoresSafeArea()
                
                ScrollView {
                    VStack(spacing: 18) {
                        // Header
                        HStack {
                            SectionHeader(
                                title: "Operations & Locations",
                                eyebrow: "MIANU-SM COMMAND",
                                subtitle: "Conference attendance & hall presence"
                            )
                            Spacer()
                            Button(action: { loadData() }) {
                                Image(systemName: "arrow.clockwise")
                                    .font(.system(size: 16))
                                    .foregroundColor(.white)
                                    .padding(10)
                                    .background(Color(hex: 0x14171E))
                                    .clipShape(Circle())
                            }
                        }
                        
                        // Telemetry Stat Cards
                        let presentCount = locations.filter { $0.isPresent }.count
                        let absentCount = locations.filter { !$0.isPresent }.count
                        let insideCount = locations.filter { $0.isInside }.count
                        let exitedCount = locations.filter { $0.isExited }.count
                        
                        HStack(spacing: 12) {
                            StatCard(
                                title: "Present in Sum",
                                value: "\(summary?.totalPresent ?? presentCount) / \(summary?.totalRegistered ?? locations.count)",
                                caption: "Rate: \(summary?.attendanceRate ?? 0)%",
                                icon: "person.crop.circle.badge.checkmark",
                                accentColor: MianuColors.success
                            )
                            StatCard(
                                title: "Awaiting Arrival",
                                value: "\(summary?.totalAbsent ?? absentCount)",
                                caption: "Absent delegates",
                                icon: "person.3.fill",
                                accentColor: MianuColors.danger
                            )
                        }
                        
                        HStack(spacing: 12) {
                            StatCard(
                                title: "Turnstile Swipes",
                                value: "\(summary?.totalSwipes ?? 0)",
                                caption: "Main gate taps",
                                icon: "creditcard.fill"
                            )
                            StatCard(
                                title: "Inside Halls",
                                value: "\(insideCount)",
                                caption: "\(exitedCount) exited rooms",
                                icon: "building.2.crop.circle",
                                accentColor: MianuColors.warning
                            )
                        }
                        
                        // Main Entrance Turnstile Scanner Card
                        VStack(spacing: 14) {
                            HStack {
                                Image(systemName: "wave.3.forward.circle.fill")
                                    .foregroundColor(MianuColors.success)
                                Text("Entrance Turnstile Scanner")
                                    .font(.system(size: 15, weight: .bold))
                                    .foregroundColor(.white)
                                Spacer()
                            }
                            
                            Picker("Action", selection: $swipeAction) {
                                Text("Check In").tag("check_in")
                                Text("Check Out").tag("check_out")
                                Text("Toggle").tag("toggle")
                            }
                            .pickerStyle(SegmentedPickerStyle())
                            
                            HStack(spacing: 8) {
                                Button(action: { startNFC() }) {
                                    HStack(spacing: 6) {
                                        Image(systemName: "sensor.tag.radiowaves.forward.fill")
                                        Text("Tap NFC Badge")
                                    }
                                    .font(.system(size: 13, weight: .bold))
                                    .foregroundColor(.black)
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 46)
                                    .background(Color.white)
                                    .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                                }
                                .buttonStyle(MianuPressableButtonStyle(scale: 0.96))
                            }
                            
                            HStack(spacing: 10) {
                                MianuTextField("Card UID", placeholder: "04A2B3C4D5", leadingIcon: "creditcard.fill", text: $cardUidInput)
                                Button(action: { recordSwipe(cardUid: cardUidInput) }) {
                                    Text("Swipe")
                                        .font(.system(size: 13, weight: .bold))
                                        .foregroundColor(.black)
                                        .frame(height: 48)
                                        .padding(.horizontal, 16)
                                        .background(Color.white)
                                        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                                }
                                .buttonStyle(MianuPressableButtonStyle(scale: 0.95))
                                .padding(.top, 22)
                                .disabled(cardUidInput.isEmpty || isSubmitting)
                            }
                            
                            if let feedback = swipeFeedback {
                                HStack {
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text("\(feedback.name) • \(feedback.action == "check_in" ? "PRESENT" : "ABSENT")")
                                            .font(.system(size: 13, weight: .bold))
                                            .foregroundColor(feedback.status == "present" ? MianuColors.success : MianuColors.danger)
                                        Text("Card: \(feedback.cardUid)")
                                            .font(.system(size: 11))
                                            .foregroundColor(MianuColors.fgMuted)
                                    }
                                    Spacer()
                                    Button(action: { swipeFeedback = nil }) {
                                        Image(systemName: "xmark")
                                            .font(.system(size: 12))
                                            .foregroundColor(MianuColors.fgMuted)
                                    }
                                }
                                .padding(10)
                                .background((feedback.status == "present" ? MianuColors.success : MianuColors.danger).opacity(0.12))
                                .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
                            }
                        }
                        .padding(18)
                        .glassPanel()
                        
                        // Bulk Actions Toolbar
                        HStack {
                            Button(action: { toggleSelectAll() }) {
                                HStack(spacing: 8) {
                                    Image(systemName: allFilteredSelected ? "checkmark.square.fill" : "square")
                                        .foregroundColor(.white)
                                    Text(selectedUserIds.isEmpty ? "Select (\(filteredList.count))" : "Selected (\(selectedUserIds.count))")
                                        .font(.system(size: 13, weight: .semibold))
                                        .foregroundColor(.white)
                                }
                            }
                            Spacer()
                            
                            if !selectedUserIds.isEmpty {
                                HStack(spacing: 8) {
                                    MianuSecondaryButton("Present", tint: MianuColors.success, height: 36) {
                                        handleBulk(status: "present", all: false)
                                    }
                                    MianuSecondaryButton("Absent", tint: MianuColors.danger, height: 36) {
                                        handleBulk(status: "absent", all: false)
                                    }
                                }
                            } else {
                                HStack(spacing: 8) {
                                    MianuSecondaryButton("All Present", tint: MianuColors.success, height: 36) {
                                        handleBulk(status: "present", all: true)
                                    }
                                    MianuSecondaryButton("Reset All", tint: MianuColors.danger, height: 36) {
                                        handleBulk(status: "absent", all: true)
                                    }
                                }
                            }
                        }
                        .padding(14)
                        .glassCard()
                        
                        // Search
                        MianuTextField("Search", placeholder: "Search attendee, team, card UID or hall...", leadingIcon: "magnifyingglass", text: $searchQuery)
                        
                        // Filter Pills
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 8) {
                                FilterPill(title: "All (\(locations.count))", isSelected: selectedFilter == "all") { selectedFilter = "all" }
                                FilterPill(title: "Present (\(presentCount))", isSelected: selectedFilter == "present") { selectedFilter = "present" }
                                FilterPill(title: "Absent (\(absentCount))", isSelected: selectedFilter == "absent") { selectedFilter = "absent" }
                                FilterPill(title: "Inside (\(insideCount))", isSelected: selectedFilter == "inside") { selectedFilter = "inside" }
                                FilterPill(title: "Exited (\(exitedCount))", isSelected: selectedFilter == "exited") { selectedFilter = "exited" }
                            }
                        }
                        
                        // Delegate List
                        if isLoading && locations.isEmpty {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                .padding(40)
                        } else if filteredList.isEmpty {
                            EmptyStateView(
                                title: "No Delegates Found",
                                description: searchQuery.isEmpty ? "No delegates match this filter." : "No results for \"\(searchQuery)\"",
                                icon: "person.2.slash.fill"
                            )
                        } else {
                            LazyVStack(spacing: 12) {
                                ForEach(filteredList) { delegate in
                                    DelegateRow(
                                        delegate: delegate,
                                        isSelected: selectedUserIds.contains(delegate.userId),
                                        onToggleSelect: {
                                            if selectedUserIds.contains(delegate.userId) {
                                                selectedUserIds.remove(delegate.userId)
                                            } else {
                                                selectedUserIds.insert(delegate.userId)
                                            }
                                        },
                                        onToggleAttendance: {
                                            toggleSingle(userId: delegate.userId, currentStatus: delegate.attendanceStatus ?? "absent")
                                        }
                                    )
                                }
                            }
                        }
                    }
                    .padding(20)
                }
                .refreshable {
                    await loadDataAsync()
                }
                .scrollDismissesKeyboard(.interactively)
            }
            .navigationBarHidden(true)
            .onAppear { loadData() }
        }
        .navigationViewStyle(StackNavigationViewStyle())
    }
    
    private func loadData() {
        Task {
            await loadDataAsync()
        }
    }

    private func loadDataAsync() async {
        guard let token = session.token else { return }
        isLoading = true
        do {
            async let locs = APIService.shared.getLocations(token: token)
            async let att = APIService.shared.getAttendance(token: token)
            let (resLocs, resAtt) = try await (locs, att)
            await MainActor.run {
                self.locations = resLocs
                self.summary = resAtt.summary
                self.isLoading = false
            }
        } catch {
            await MainActor.run {
                self.errorMessage = error.localizedDescription
                self.isLoading = false
            }
        }
    }
    
    private func startNFC() {
        nfcReader.scan(alertMessage: "Tap delegate badge to turnstile") { uid in
            recordSwipe(cardUid: uid)
        }
    }
    
    private func recordSwipe(cardUid: String) {
        guard let token = session.token, !cardUid.isEmpty else { return }
        isSubmitting = true
        Task {
            do {
                let req = AttendanceSwipeRequest(cardUid: cardUid.trimmingCharacters(in: .whitespacesAndNewlines), action: swipeAction)
                let res = try await APIService.shared.recordAttendanceSwipe(request: req, token: token)
                await MainActor.run {
                    self.swipeFeedback = res.user
                    self.summary = res.summary
                    self.cardUidInput = ""
                    self.isSubmitting = false
                    UINotificationFeedbackGenerator().notificationOccurred(.success)
                    self.loadData()
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
    
    private func toggleSingle(userId: String, currentStatus: String) {
        guard let token = session.token else { return }
        let targetStatus = (currentStatus == "present") ? "absent" : "present"
        Task {
            do {
                let req = SingleAttendanceRequest(userId: userId, status: targetStatus)
                _ = try await APIService.shared.updateSingleAttendance(request: req, token: token)
                await MainActor.run {
                    UINotificationFeedbackGenerator().notificationOccurred(.success)
                    self.loadData()
                }
            } catch {
                await MainActor.run {
                    self.errorMessage = error.localizedDescription
                    UINotificationFeedbackGenerator().notificationOccurred(.error)
                }
            }
        }
    }
    
    private func handleBulk(status: String, all: Bool) {
        guard let token = session.token else { return }
        isSubmitting = true
        let ids = all ? nil : Array(selectedUserIds)
        Task {
            do {
                let req = BulkAttendanceRequest(userIds: ids, all: all ? true : nil, status: status)
                _ = try await APIService.shared.updateBulkAttendance(request: req, token: token)
                await MainActor.run {
                    if !all { self.selectedUserIds.removeAll() }
                    self.isSubmitting = false
                    UINotificationFeedbackGenerator().notificationOccurred(.success)
                    self.loadData()
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
    
    private func toggleSelectAll() {
        if allFilteredSelected {
            for item in filteredList {
                selectedUserIds.remove(item.userId)
            }
        } else {
            for item in filteredList {
                selectedUserIds.insert(item.userId)
            }
        }
    }
}

// MARK: - Delegate Row
private struct DelegateRow: View {
    let delegate: DelegateLocation
    let isSelected: Bool
    let onToggleSelect: () -> Void
    let onToggleAttendance: () -> Void
    
    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            Button(action: onToggleSelect) {
                Image(systemName: isSelected ? "checkmark.square.fill" : "square")
                    .font(.system(size: 18))
                    .foregroundColor(isSelected ? .white : MianuColors.fgMuted)
            }
            .padding(.top, 4)
            
            AvatarView(
                initials: delegate.name.split(separator: " ").prefix(2).compactMap { $0.first.map { String($0) } }.joined(),
                size: 42,
                presenceColor: delegate.isPresent ? MianuColors.success : MianuColors.danger
            )
            
            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(delegate.name)
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(.white)
                    Spacer()
                    StatusPill(
                        text: delegate.isPresent ? "PRESENT" : "ABSENT",
                        color: delegate.isPresent ? MianuColors.success : MianuColors.danger
                    )
                }
                
                HStack {
                    Text(delegate.teamName ?? "Individual (\(delegate.role))")
                        .font(.system(size: 12))
                        .foregroundColor(MianuColors.fgMuted)
                    Spacer()
                    Text(delegate.statusDisplay)
                        .font(.system(size: 11, weight: .medium))
                        .foregroundColor(delegate.isInside ? MianuColors.success : (delegate.isExited ? MianuColors.warning : MianuColors.fgMuted))
                }
                
                HStack {
                    if let uid = delegate.cardUid, !uid.isEmpty {
                        Text("UID: \(uid)")
                            .font(.system(size: 11, design: .monospaced))
                            .foregroundColor(MianuColors.fgSubtle)
                    }
                    Spacer()
                    if let time = delegate.attendanceTime {
                        Text(String(time.prefix(16)).replacingOccurrences(of: "T", with: " "))
                            .font(.system(size: 10))
                            .foregroundColor(MianuColors.fgSubtle)
                    }
                }
                .padding(.top, 2)
                
                HStack {
                    Spacer()
                    MianuSecondaryButton(
                        delegate.isPresent ? "Mark Absent" : "Mark Present",
                        tint: delegate.isPresent ? MianuColors.danger : MianuColors.success,
                        height: 32,
                        action: onToggleAttendance
                    )
                }
                .padding(.top, 4)
            }
        }
        .padding(14)
        .glassCard()
    }
}
