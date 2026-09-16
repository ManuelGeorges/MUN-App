import Foundation

// MARK: - Roles
public enum UserRole: String, Codable, CaseIterable, Identifiable {
    case admin = "admin"
    case chiefOrganizer = "chief_organizer"
    case organizer = "organizer"
    case teamLeader = "team_leader"
    case teamMember = "team_member"
    case user = "user"
    
    public var id: String { rawValue }
    
    public var displayName: String {
        switch self {
        case .admin: return "Admin"
        case .chiefOrganizer: return "Chief Organizer"
        case .organizer: return "Organizer"
        case .teamLeader: return "Team Leader"
        case .teamMember: return "Team Member"
        case .user: return "Delegate"
        }
    }
}

public enum TeamRole: String, Codable {
    case leader = "leader"
    case member = "member"
}

// MARK: - Auth Models
public struct LoginRequest: Codable {
    public let username: String
    public let password: String
    
    public init(username: String, password: String) {
        self.username = username
        self.password = password
    }
}

public struct RegisterRequest: Codable {
    public let username: String
    public let email: String
    public let password: String
    public let role: UserRole
}

public struct TokenPair: Codable {
    public let accessToken: String
    public let refreshToken: String
    public let expiresIn: Int
    public let role: UserRole
    public let userId: String?
    public let name: String?
    public let teamId: String?
    public let teamRole: TeamRole?
    public let permissions: [String]?
    
    enum CodingKeys: String, CodingKey {
        case accessToken = "access_token"
        case refreshToken = "refresh_token"
        case expiresIn = "expires_in"
        case role
        case userId = "user_id"
        case name
        case teamId = "team_id"
        case teamRole = "team_role"
        case permissions
    }
}

// MARK: - User Models
public struct UserResponse: Codable, Identifiable, Hashable {
    public let id: String
    public let name: String
    public let email: String?
    public let phone: String?
    public let tokenBalance: Double?
    public let mealAllowance: Int?
    public let mealsBalance: Int?
    public let isActive: Bool?
    public let role: String
    public let teamId: String?
    public let teamRole: TeamRole?
    public let cardUid: String?
    
    enum CodingKeys: String, CodingKey {
        case id, name, email, phone
        case tokenBalance = "token_balance"
        case mealAllowance = "meal_allowance"
        case mealsBalance = "meals_balance"
        case isActive = "is_active"
        case role
        case teamId = "team_id"
        case teamRole = "team_role"
        case cardUid = "card_uid"
    }
    
    public var roleEnum: UserRole {
        UserRole(rawValue: role.lowercased()) ?? .user
    }
    
    public var initials: String {
        let parts = name.trimmingCharacters(in: .whitespacesAndNewlines).split(separator: " ")
        if parts.isEmpty { return "?" }
        let first = parts.prefix(2).compactMap { $0.first.map { String($0).uppercased() } }.joined()
        return first.isEmpty ? "?" : first
    }
}

public struct UserCreate: Codable {
    public let name: String
    public let email: String?
    public let phone: String?
    public let password: String?
    public let role: UserRole
    public let teamId: String?
    public let teamRole: TeamRole?
    public let mealAllowance: Int
    public let mealsBalance: Int
    
    enum CodingKeys: String, CodingKey {
        case name, email, phone, password, role
        case teamId = "team_id"
        case teamRole = "team_role"
        case mealAllowance = "meal_allowance"
        case mealsBalance = "meals_balance"
    }
    
    public init(name: String, email: String? = nil, phone: String? = nil, password: String? = nil, role: UserRole = .user, teamId: String? = nil, teamRole: TeamRole? = nil, mealAllowance: Int = 6, mealsBalance: Int = 6) {
        self.name = name
        self.email = email
        self.phone = phone
        self.password = password
        self.role = role
        self.teamId = teamId
        self.teamRole = teamRole
        self.mealAllowance = mealAllowance
        self.mealsBalance = mealsBalance
    }
}

public struct UserUpdate: Codable {
    public let name: String?
    public let email: String?
    public let phone: String?
    public let isActive: Bool?
    public let mealAllowance: Int?
    public let mealsBalance: Int?
    public let role: UserRole?
    public let teamId: String?
    public let teamRole: TeamRole?
    
    enum CodingKeys: String, CodingKey {
        case name, email, phone
        case isActive = "is_active"
        case mealAllowance = "meal_allowance"
        case mealsBalance = "meals_balance"
        case role
        case teamId = "team_id"
        case teamRole = "team_role"
    }
    
    public init(name: String? = nil, email: String? = nil, phone: String? = nil, isActive: Bool? = nil, mealAllowance: Int? = nil, mealsBalance: Int? = nil, role: UserRole? = nil, teamId: String? = nil, teamRole: TeamRole? = nil) {
        self.name = name
        self.email = email
        self.phone = phone
        self.isActive = isActive
        self.mealAllowance = mealAllowance
        self.mealsBalance = mealsBalance
        self.role = role
        self.teamId = teamId
        self.teamRole = teamRole
    }
}

public struct MealAdjustRequest: Codable {
    public let delta: Int?
    public let mealsBalance: Int?
    
    enum CodingKeys: String, CodingKey {
        case delta
        case mealsBalance = "meals_balance"
    }
    
    public init(delta: Int? = nil, mealsBalance: Int? = nil) {
        self.delta = delta
        self.mealsBalance = mealsBalance
    }
}

public struct CardLinkRequest: Codable {
    public let userId: String
    public let cardUid: String
    
    enum CodingKeys: String, CodingKey {
        case userId = "user_id"
        case cardUid = "card_uid"
    }
}

public struct CardResponse: Codable {
    public let cardUid: String
    public let user: UserResponse
    
    enum CodingKeys: String, CodingKey {
        case cardUid = "card_uid"
        case user
    }
}

// MARK: - Attendance Models
public struct AttendanceSummary: Codable {
    public let totalRegistered: Int
    public let totalPresent: Int
    public let totalAbsent: Int
    public let attendanceRate: Int
    public let totalSwipes: Int
    
    enum CodingKeys: String, CodingKey {
        case totalRegistered = "total_registered"
        case totalPresent = "total_present"
        case totalAbsent = "total_absent"
        case attendanceRate = "attendance_rate"
        case totalSwipes = "total_swipes"
    }
}

public struct AttendeeRecord: Codable, Identifiable {
    public let userId: String
    public let name: String
    public let email: String?
    public let phone: String?
    public let role: String
    public let teamId: String?
    public let teamName: String?
    public let cardUid: String?
    public let status: String // "present" | "absent"
    public let lastCheckIn: String?
    public let lastCheckOut: String?
    public let method: String?
    public let updatedAt: String?
    
    public var id: String { userId }
    public var isPresent: Bool { status == "present" }
    
    enum CodingKeys: String, CodingKey {
        case userId = "user_id"
        case name, email, phone, role
        case teamId = "team_id"
        case teamName = "team_name"
        case cardUid = "card_uid"
        case status
        case lastCheckIn = "last_check_in"
        case lastCheckOut = "last_check_out"
        case method
        case updatedAt = "updated_at"
    }
}

public struct AttendanceResponse: Codable {
    public let summary: AttendanceSummary
    public let attendees: [AttendeeRecord]
}

public struct AttendanceSwipeRequest: Codable {
    public let cardUid: String
    public let action: String // "check_in" | "check_out" | "toggle"
    
    enum CodingKeys: String, CodingKey {
        case cardUid = "card_uid"
        case action
    }
    
    public init(cardUid: String, action: String = "check_in") {
        self.cardUid = cardUid
        self.action = action
    }
}

public struct AttendanceSwipeUser: Codable {
    public let id: String
    public let name: String
    public let role: String
    public let teamName: String?
    public let cardUid: String
    public let status: String
    public let action: String
    public let timestamp: String
    
    enum CodingKeys: String, CodingKey {
        case id, name, role
        case teamName = "team_name"
        case cardUid = "card_uid"
        case status, action, timestamp
    }
}

public struct AttendanceSwipeResponse: Codable {
    public let success: Bool
    public let user: AttendanceSwipeUser
    public let summary: AttendanceSummary
}

public struct SingleAttendanceRequest: Codable {
    public let userId: String
    public let status: String // "present" | "absent"
    
    enum CodingKeys: String, CodingKey {
        case userId = "user_id"
        case status
    }
    
    public init(userId: String, status: String) {
        self.userId = userId
        self.status = status
    }
}

public struct SingleAttendanceResponse: Codable {
    public let success: Bool
    public let userId: String
    public let name: String
    public let status: String
    public let timestamp: String
    
    enum CodingKeys: String, CodingKey {
        case success
        case userId = "user_id"
        case name, status, timestamp
    }
}

public struct BulkAttendanceRequest: Codable {
    public let userIds: [String]?
    public let all: Bool?
    public let status: String // "present" | "absent"
    
    enum CodingKeys: String, CodingKey {
        case userIds = "user_ids"
        case all, status
    }
    
    public init(userIds: [String]? = nil, all: Bool? = nil, status: String) {
        self.userIds = userIds
        self.all = all
        self.status = status
    }
}

public struct BulkAttendanceResponse: Codable {
    public let success: Bool
    public let count: Int
    public let status: String
    public let timestamp: String?
}

// MARK: - Location Models
public struct DelegateLocation: Codable, Identifiable {
    public let userId: String
    public let name: String
    public let role: String
    public let teamId: String?
    public let teamName: String?
    public let cardUid: String?
    public let currentHallId: String?
    public let currentHallName: String?
    public let status: String // "inside", "exited", "never_scanned"
    public let lastAction: String?
    public let lastSeen: String?
    public let attendanceStatus: String?
    public let attendanceTime: String?
    public let attendanceMethod: String?
    
    public var id: String { userId }
    public var isInside: Bool { status == "inside" }
    public var isExited: Bool { status == "exited" }
    public var isPresent: Bool { (attendanceStatus ?? "absent") == "present" }
    
    public var statusDisplay: String {
        switch status {
        case "inside": return currentHallName ?? "Inside Hall"
        case "exited": return "Exited"
        default: return "Not Checked In"
        }
    }
    
    enum CodingKeys: String, CodingKey {
        case userId = "user_id"
        case name, role
        case teamId = "team_id"
        case teamName = "team_name"
        case cardUid = "card_uid"
        case currentHallId = "current_hall_id"
        case currentHallName = "current_hall_name"
        case status
        case lastAction = "last_action"
        case lastSeen = "last_seen"
        case attendanceStatus = "attendance_status"
        case attendanceTime = "attendance_time"
        case attendanceMethod = "attendance_method"
    }
}

public struct LiveScanEvent: Codable, Identifiable {
    public let id: String
    public let type: String // "meal" | "access" | "attendance"
    public let userId: String?
    public let userName: String?
    public let cardUid: String?
    public let detail: String?
    public let timestamp: String
    public let status: String?
}

// MARK: - Hall Models
public enum AccessAction: String, Codable {
    case entry = "entry"
    case exit = "exit"
}

public struct HallCreate: Codable {
    public let name: String
    public let capacityThreshold: Int
    public let allowedRoles: [UserRole]
    
    enum CodingKeys: String, CodingKey {
        case name
        case capacityThreshold = "capacity_threshold"
        case allowedRoles = "allowed_roles"
    }
}

public struct HallUpdate: Codable {
    public let name: String?
    public let capacityThreshold: Int?
    public let allowedRoles: [UserRole]?
    
    enum CodingKeys: String, CodingKey {
        case name
        case capacityThreshold = "capacity_threshold"
        case allowedRoles = "allowed_roles"
    }
}

public struct HallResponse: Codable, Identifiable {
    public let id: String
    public let name: String
    public let capacityThreshold: Int
    public let currentOccupancy: Int
    public let allowedRoles: [UserRole]?
    
    public var occupancyRatio: Double {
        guard capacityThreshold > 0 else { return 0 }
        return min(max(Double(currentOccupancy) / Double(capacityThreshold), 0.0), 1.0)
    }
    public var isAtCapacity: Bool { currentOccupancy >= capacityThreshold }
    public var isNearCapacity: Bool { !isAtCapacity && occupancyRatio >= 0.8 }
    
    enum CodingKeys: String, CodingKey {
        case id, name
        case capacityThreshold = "capacity_threshold"
        case currentOccupancy = "current_occupancy"
        case allowedRoles = "allowed_roles"
    }
}

public struct HallAvailability: Codable, Identifiable {
    public let id: String
    public let name: String
    public let capacity: Int
    public let occupancy: Int
    public let seatsFree: Int
    public let state: String // "empty", "available", "filling", "full"
    
    public var occupancyRatio: Double {
        guard capacity > 0 else { return 0 }
        return min(max(Double(occupancy) / Double(capacity), 0.0), 1.0)
    }
    
    enum CodingKeys: String, CodingKey {
        case id, name, capacity, occupancy
        case seatsFree = "seats_free"
        case state
    }
}

public struct AccessScanRequest: Codable {
    public let cardUid: String
    public let hallId: String
    public let action: AccessAction
    
    enum CodingKeys: String, CodingKey {
        case cardUid = "card_uid"
        case hallId = "hall_id"
        case action
    }
    
    public init(cardUid: String, hallId: String, action: AccessAction) {
        self.cardUid = cardUid
        self.hallId = hallId
        self.action = action
    }
}

public struct AccessLogResponse: Codable, Identifiable {
    public let id: String
    public let userId: String
    public let hallId: String
    public let action: AccessAction
    public let timestamp: String
    public let allowed: Bool
    public let reason: String?
    
    enum CodingKeys: String, CodingKey {
        case id
        case userId = "user_id"
        case hallId = "hall_id"
        case action, timestamp, allowed, reason
    }
}

// MARK: - Meal Models
public enum MealType: String, Codable, CaseIterable {
    case breakfast = "breakfast"
    case lunch = "lunch"
    
    public var displayName: String {
        switch self {
        case .breakfast: return "Breakfast"
        case .lunch: return "Lunch"
        }
    }
}

public struct MealSwipeRequest: Codable {
    public let cardUid: String
    public let mealType: MealType
    
    enum CodingKeys: String, CodingKey {
        case cardUid = "card_uid"
        case mealType = "meal_type"
    }
    
    public init(cardUid: String, mealType: MealType) {
        self.cardUid = cardUid
        self.mealType = mealType
    }
}

public struct MealSwipeResponse: Codable, Identifiable {
    public let id: String
    public let userId: String
    public let mealType: MealType
    public let swipeTimestamp: String
    public let mealsRemaining: Int?
    public let userName: String?
    
    enum CodingKeys: String, CodingKey {
        case id
        case userId = "user_id"
        case mealType = "meal_type"
        case swipeTimestamp = "swipe_timestamp"
        case mealsRemaining = "meals_remaining"
        case userName = "user_name"
    }
}

public struct MealWindowRequest: Codable {
    public let breakfastStart: String
    public let breakfastEnd: String
    public let lunchStart: String
    public let lunchEnd: String
    
    enum CodingKeys: String, CodingKey {
        case breakfastStart = "breakfast_start"
        case breakfastEnd = "breakfast_end"
        case lunchStart = "lunch_start"
        case lunchEnd = "lunch_end"
    }
    
    public static let `default` = MealWindowRequest(
        breakfastStart: "07:00",
        breakfastEnd: "10:00",
        lunchStart: "12:00",
        lunchEnd: "15:00"
    )
}

// MARK: - Team Models
public struct TeamResponse: Codable, Identifiable {
    public let id: String
    public let name: String
    public let capacity: Int
    public let currentSize: Int
    
    public var isFull: Bool { currentSize >= capacity }
    public var occupancyRatio: Double {
        guard capacity > 0 else { return 0 }
        return min(max(Double(currentSize) / Double(capacity), 0.0), 1.0)
    }
    
    enum CodingKeys: String, CodingKey {
        case id, name, capacity
        case currentSize = "current_size"
    }
}

public struct TeamCreate: Codable {
    public let name: String
    public let capacity: Int
    
    public init(name: String, capacity: Int) {
        self.name = name
        self.capacity = capacity
    }
}

public struct TeamUpdate: Codable {
    public let name: String?
    public let capacity: Int?
    
    public init(name: String? = nil, capacity: Int? = nil) {
        self.name = name
        self.capacity = capacity
    }
}

public enum BroadcastAudience: String, Codable, CaseIterable {
    case all = "all"
    case team = "team"
    case participant = "participant"
    
    public var label: String {
        switch self {
        case .all: return "Everyone"
        case .team: return "A Team"
        case .participant: return "One Person"
        }
    }
}

public struct BroadcastCreate: Codable {
    public let message: String
    public let audience: BroadcastAudience
    public let teamId: String?
    public let recipientId: String?
    
    enum CodingKeys: String, CodingKey {
        case message, audience
        case teamId = "team_id"
        case recipientId = "recipient_id"
    }
    
    public init(message: String, audience: BroadcastAudience = .all, teamId: String? = nil, recipientId: String? = nil) {
        self.message = message
        self.audience = audience
        self.teamId = teamId
        self.recipientId = recipientId
    }
}

public struct NotificationResponse: Codable, Identifiable {
    public let id: String
    public let senderId: String
    public let senderName: String?
    public let audience: BroadcastAudience?
    public let teamId: String?
    public let teamName: String?
    public let recipientId: String?
    public let recipientName: String?
    public let message: String
    public let timestamp: String
    public let recipientCount: Int?
    
    public var audienceLabel: String {
        switch audience {
        case .all: return "Everyone"
        case .team: return teamName ?? "Team"
        case .participant: return recipientName ?? "Direct"
        default: return "Broadcast"
        }
    }
    
    enum CodingKeys: String, CodingKey {
        case id
        case senderId = "sender_id"
        case senderName = "sender_name"
        case audience
        case teamId = "team_id"
        case teamName = "team_name"
        case recipientId = "recipient_id"
        case recipientName = "recipient_name"
        case message, timestamp
        case recipientCount = "recipient_count"
    }
}

// MARK: - Analytics & Reports Models
public struct DashboardOverview: Codable {
    public let totalMealsToday: Int
    public let totalAccessScansToday: Int
    public let activeUsers: Int
    public let peopleInsideHalls: Int
    public let totalTransactionsToday: Int?
    public let totalTokenBalance: Double?
    
    enum CodingKeys: String, CodingKey {
        case totalMealsToday = "total_meals_today"
        case totalAccessScansToday = "total_access_scans_today"
        case activeUsers = "active_users"
        case peopleInsideHalls = "people_inside_halls"
        case totalTransactionsToday = "total_transactions_today"
        case totalTokenBalance = "total_token_balance"
    }
}

public struct AnalyticsSummary: Codable {
    public let metric: String
    public let total: Int
    public let generatedAt: String
    
    enum CodingKeys: String, CodingKey {
        case metric, total
        case generatedAt = "generated_at"
    }
}

public struct ReportSummary: Codable {
    public let totalRecords: Int
    public let generatedAt: String
    
    enum CodingKeys: String, CodingKey {
        case totalRecords = "total_records"
        case generatedAt = "generated_at"
    }
}
