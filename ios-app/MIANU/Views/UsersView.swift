import SwiftUI

public struct UsersView: View {
    @ObservedObject private var session = AppSession.shared
    
    @State private var users: [UserResponse] = []
    @State private var isLoading = false
    @State private var searchQuery = ""
    @State private var selectedRole = "all"
    @State private var showingAddUser = false
    @State private var userToAdjustMeals: UserResponse? = nil
    @State private var errorMessage: String? = nil
    
    public init() {}
    
    private var filteredUsers: [UserResponse] {
        users.filter { user in
            let matchesRole = (selectedRole == "all") || (user.role.lowercased() == selectedRole)
            let q = searchQuery.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
            let matchesQuery = q.isEmpty ||
                user.name.lowercased().contains(q) ||
                (user.email?.lowercased().contains(q) ?? false) ||
                (user.cardUid?.lowercased().contains(q) ?? false)
            return matchesRole && matchesQuery
        }
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
                                title: "Delegates Directory",
                                eyebrow: "ROSTER & PASSES",
                                subtitle: "Manage credentials, roles & catering tokens"
                            )
                            Spacer()
                            Button(action: { showingAddUser = true }) {
                                Image(systemName: "plus")
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(.black)
                                    .padding(10)
                                    .background(Color.white)
                                    .clipShape(Circle())
                            }
                        }
                        
                        // Search
                        MianuTextField("Search", placeholder: "Search name, email or badge UID...", leadingIcon: "magnifyingglass", text: $searchQuery)
                        
                        // Role Filters
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 8) {
                                FilterPill(title: "All (\(users.count))", isSelected: selectedRole == "all") { selectedRole = "all" }
                                FilterPill(title: "Admins", isSelected: selectedRole == "admin") { selectedRole = "admin" }
                                FilterPill(title: "Organizers", isSelected: selectedRole == "organizer") { selectedRole = "organizer" }
                                FilterPill(title: "Leaders", isSelected: selectedRole == "team_leader") { selectedRole = "team_leader" }
                                FilterPill(title: "Delegates", isSelected: selectedRole == "user") { selectedRole = "user" }
                            }
                        }
                        
                        // List
                        if isLoading && users.isEmpty {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                .padding(40)
                        } else if filteredUsers.isEmpty {
                            EmptyStateView(
                                title: "No Delegates Found",
                                description: searchQuery.isEmpty ? "No users match this role." : "No results for \"\(searchQuery)\"",
                                icon: "person.slash.fill"
                            )
                        } else {
                            LazyVStack(spacing: 10) {
                                ForEach(filteredUsers) { user in
                                    UserRow(
                                        user: user,
                                        onAdjustMeals: { userToAdjustMeals = user }
                                    )
                                }
                            }
                        }
                    }
                    .padding(20)
                }
                .refreshable {
                    await loadUsersAsync()
                }
                .scrollDismissesKeyboard(.interactively)
            }
            .navigationBarHidden(true)
            .sheet(isPresented: $showingAddUser) {
                AddUserSheet(onCreated: { loadUsers() })
            }
            .sheet(item: $userToAdjustMeals) { user in
                AdjustMealsSheet(user: user, onUpdated: { loadUsers() })
            }
            .onAppear { loadUsers() }
        }
        .navigationViewStyle(StackNavigationViewStyle())
    }
    
    private func loadUsers() {
        Task {
            await loadUsersAsync()
        }
    }

    private func loadUsersAsync() async {
        guard let token = session.token else { return }
        isLoading = true
        do {
            let res = try await APIService.shared.getUsers(token: token)
            await MainActor.run {
                self.users = res
                self.isLoading = false
            }
        } catch {
            await MainActor.run {
                self.errorMessage = error.localizedDescription
                self.isLoading = false
            }
        }
    }
}

// MARK: - User Row
private struct UserRow: View {
    let user: UserResponse
    let onAdjustMeals: () -> Void
    
    var body: some View {
        HStack(spacing: 12) {
            AvatarView(initials: user.initials, size: 44)
            
            VStack(alignment: .leading, spacing: 3) {
                HStack {
                    Text(user.name)
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(.white)
                    Spacer()
                    StatusPill(text: user.role.uppercased(), color: roleColor(user.role))
                }
                
                if let email = user.email {
                    Text(email)
                        .font(.system(size: 12))
                        .foregroundColor(MianuColors.fgMuted)
                }
                
                HStack {
                    if let uid = user.cardUid {
                        Text("UID: \(uid)")
                            .font(.system(size: 11, design: .monospaced))
                            .foregroundColor(MianuColors.fgSubtle)
                    }
                    Spacer()
                    Button(action: onAdjustMeals) {
                        Text("Meals: \(user.mealsBalance ?? 6)")
                            .font(.system(size: 11, weight: .semibold))
                            .foregroundColor(MianuColors.warning)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background(MianuColors.warning.opacity(0.12))
                            .clipShape(Capsule())
                    }
                }
            }
        }
        .padding(14)
        .glassCard()
    }
    
    private func roleColor(_ role: String) -> Color {
        switch role.lowercased() {
        case "admin", "chief_organizer": return MianuColors.danger
        case "organizer": return MianuColors.info
        case "team_leader": return MianuColors.warning
        default: return MianuColors.success
        }
    }
}

// MARK: - Add User Sheet
private struct AddUserSheet: View {
    @Environment(\.presentationMode) var presentationMode
    @ObservedObject private var session = AppSession.shared
    
    let onCreated: () -> Void
    
    @State private var name = ""
    @State private var email = ""
    @State private var password = ""
    @State private var role: UserRole = .user
    @State private var isSubmitting = false
    @State private var errorText: String? = nil
    
    var body: some View {
        NavigationView {
            ZStack {
                MianuColors.bg.ignoresSafeArea()
                
                ScrollView {
                    VStack(spacing: 16) {
                        MianuTextField("Full Name", placeholder: "Jean Dupont", text: $name)
                        MianuTextField("Email", placeholder: "jean.dupont@delegate.org", text: $email)
                        MianuTextField("Password", placeholder: "Temporary password", text: $password, isSecure: true)
                        
                        VStack(alignment: .leading, spacing: 6) {
                            Text("ROLE")
                                .font(.system(size: 12, weight: .medium))
                                .foregroundColor(MianuColors.fgMuted)
                            Picker("Role", selection: $role) {
                                ForEach(UserRole.allCases) { r in
                                    Text(r.displayName).tag(r)
                                }
                            }
                            .pickerStyle(SegmentedPickerStyle())
                        }
                        
                        if let err = errorText {
                            Text(err)
                                .font(.system(size: 12))
                                .foregroundColor(MianuColors.danger)
                        }
                        
                        MianuButton("Create Delegate", isLoading: isSubmitting, isEnabled: !name.isEmpty && !email.isEmpty) {
                            createUser()
                        }
                        .padding(.top, 10)
                    }
                    .padding(20)
                }
            }
            .navigationTitle("New Delegate")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { presentationMode.wrappedValue.dismiss() }
                        .foregroundColor(.white)
                }
            }
        }
    }
    
    private func createUser() {
        guard let token = session.token else { return }
        isSubmitting = true
        errorText = nil
        
        Task {
            do {
                let user = UserCreate(name: name, email: email, password: password.isEmpty ? "Delegate@2026" : password, role: role)
                _ = try await APIService.shared.createUser(user: user, token: token)
                await MainActor.run {
                    self.isSubmitting = false
                    self.onCreated()
                    self.presentationMode.wrappedValue.dismiss()
                }
            } catch {
                await MainActor.run {
                    self.errorText = error.localizedDescription
                    self.isSubmitting = false
                }
            }
        }
    }
}

// MARK: - Adjust Meals Sheet
private struct AdjustMealsSheet: View {
    @Environment(\.presentationMode) var presentationMode
    @ObservedObject private var session = AppSession.shared
    
    let user: UserResponse
    let onUpdated: () -> Void
    
    @State private var balanceText = ""
    @State private var isSubmitting = false
    
    var body: some View {
        NavigationView {
            ZStack {
                MianuColors.bg.ignoresSafeArea()
                
                VStack(spacing: 20) {
                    Text("Adjust Meals for \(user.name)")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.white)
                    
                    MianuTextField("New Meals Balance", placeholder: "e.g. 6", text: $balanceText)
                    
                    MianuButton("Save Allowance", isLoading: isSubmitting) {
                        saveMeals()
                    }
                    
                    Spacer()
                }
                .padding(24)
            }
            .navigationTitle("Adjust Meals")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { presentationMode.wrappedValue.dismiss() }
                        .foregroundColor(.white)
                }
            }
            .onAppear {
                balanceText = "\(user.mealsBalance ?? 6)"
            }
        }
    }
    
    private func saveMeals() {
        guard let token = session.token, let newBalance = Int(balanceText) else { return }
        isSubmitting = true
        Task {
            do {
                let req = MealAdjustRequest(mealsBalance: newBalance)
                _ = try await APIService.shared.adjustUserMeals(userId: user.id, body: req, token: token)
                await MainActor.run {
                    self.isSubmitting = false
                    self.onUpdated()
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
