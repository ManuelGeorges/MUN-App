import Foundation
import SwiftUI

@MainActor
public final class AppSession: ObservableObject {
    public static let shared = AppSession()
    
    private let kAccessTokenKey = "mianu_access_token"
    private let kRefreshTokenKey = "mianu_refresh_token"
    private let kUserRoleKey = "mianu_user_role"
    private let kUserIdKey = "mianu_user_id"
    private let kUserNameKey = "mianu_user_name"
    private let kTeamIdKey = "mianu_team_id"
    
    @Published public var isAuthenticated: Bool = false
    @Published public var token: String? = nil
    @Published public var refreshToken: String? = nil
    @Published public var currentRole: UserRole = .user
    @Published public var currentUserId: String? = nil
    @Published public var currentUserName: String? = nil
    @Published public var currentTeamId: String? = nil
    @Published public var currentUser: UserResponse? = nil
    
    private init() {
        restoreSession()
    }
    
    public func setSession(from pair: TokenPair) {
        self.token = pair.accessToken
        self.refreshToken = pair.refreshToken
        self.currentRole = pair.role
        self.currentUserId = pair.userId
        self.currentUserName = pair.name
        self.currentTeamId = pair.teamId
        self.isAuthenticated = true
        
        UserDefaults.standard.set(pair.accessToken, forKey: kAccessTokenKey)
        UserDefaults.standard.set(pair.refreshToken, forKey: kRefreshTokenKey)
        UserDefaults.standard.set(pair.role.rawValue, forKey: kUserRoleKey)
        UserDefaults.standard.set(pair.userId, forKey: kUserIdKey)
        UserDefaults.standard.set(pair.name, forKey: kUserNameKey)
        UserDefaults.standard.set(pair.teamId, forKey: kTeamIdKey)
        
        Task {
            await fetchCurrentUserDetails()
        }
    }
    
    public func restoreSession() {
        guard let token = UserDefaults.standard.string(forKey: kAccessTokenKey), !token.isEmpty else {
            self.isAuthenticated = false
            return
        }
        
        self.token = token
        self.refreshToken = UserDefaults.standard.string(forKey: kRefreshTokenKey)
        if let roleStr = UserDefaults.standard.string(forKey: kUserRoleKey),
           let role = UserRole(rawValue: roleStr) {
            self.currentRole = role
        } else {
            self.currentRole = .user
        }
        self.currentUserId = UserDefaults.standard.string(forKey: kUserIdKey)
        self.currentUserName = UserDefaults.standard.string(forKey: kUserNameKey)
        self.currentTeamId = UserDefaults.standard.string(forKey: kTeamIdKey)
        self.isAuthenticated = true
        
        Task {
            await fetchCurrentUserDetails()
        }
    }
    
    public func logout() {
        self.token = nil
        self.refreshToken = nil
        self.currentRole = .user
        self.currentUserId = nil
        self.currentUserName = nil
        self.currentTeamId = nil
        self.currentUser = nil
        self.isAuthenticated = false
        
        UserDefaults.standard.removeObject(forKey: kAccessTokenKey)
        UserDefaults.standard.removeObject(forKey: kRefreshTokenKey)
        UserDefaults.standard.removeObject(forKey: kUserRoleKey)
        UserDefaults.standard.removeObject(forKey: kUserIdKey)
        UserDefaults.standard.removeObject(forKey: kUserNameKey)
        UserDefaults.standard.removeObject(forKey: kTeamIdKey)
    }
    
    public func fetchCurrentUserDetails() async {
        guard let token = token, let userId = currentUserId else { return }
        do {
            let user = try await APIService.shared.getUser(userId: userId, token: token)
            self.currentUser = user
        } catch {
            print("Failed to fetch user details: \(error)")
        }
    }
}
