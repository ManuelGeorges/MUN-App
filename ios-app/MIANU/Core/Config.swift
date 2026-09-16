import Foundation

public final class Config: ObservableObject {
    public static let shared = Config()
    
    private let kBaseURLKey = "mianu_api_base_url"
    public static let defaultBaseURL = "https://mianu-backend.karimshacker1234.workers.dev"
    
    @Published public var baseURL: String {
        didSet {
            UserDefaults.standard.set(baseURL, forKey: kBaseURLKey)
        }
    }
    
    private init() {
        if let stored = UserDefaults.standard.string(forKey: kBaseURLKey), !stored.isEmpty {
            self.baseURL = stored
        } else {
            self.baseURL = Self.defaultBaseURL
        }
    }
    
    public func resetToDefault() {
        self.baseURL = Self.defaultBaseURL
    }
}
