import Foundation
import CoreNFC
import UIKit

@MainActor
public final class NFCReader: NSObject, ObservableObject {
    public static let shared = NFCReader()
    
    @Published public var isScanning = false
    @Published public var lastScannedUID: String? = nil
    @Published public var errorMessage: String? = nil
    
    private var tagSession: NFCTagReaderSession?
    private var onScanCompletion: ((String) -> Void)?
    
    public var isNFCAvailable: Bool {
        NFCTagReaderSession.readingAvailable
    }
    
    public func scan(
        alertMessage: String = "Hold your iPhone near delegate NFC badge",
        completion: @escaping (String) -> Void
    ) {
        guard isNFCAvailable else {
            self.errorMessage = "NFC is not supported on this device"
            return
        }
        
        self.onScanCompletion = completion
        self.errorMessage = nil
        self.isScanning = true
        
        tagSession = NFCTagReaderSession(
            pollingOption: [.iso14443, .iso15693, .pace],
            delegate: self,
            queue: nil
        )
        tagSession?.alertMessage = alertMessage
        tagSession?.begin()
    }
    
    public func cancel() {
        tagSession?.invalidate()
        tagSession = nil
        isScanning = false
    }
    
    private func triggerHapticFeedback(success: Bool) {
        let generator = UINotificationFeedbackGenerator()
        generator.notificationOccurred(success ? .success : .error)
    }
}

// MARK: - NFCTagReaderSessionDelegate
extension NFCReader: NFCTagReaderSessionDelegate {
    public nonisolated func tagReaderSessionDidBecomeActive(_ session: NFCTagReaderSession) {
        // Active
    }
    
    public nonisolated func tagReaderSession(_ session: NFCTagReaderSession, didInvalidateWithError error: Error) {
        Task { @MainActor in
            self.isScanning = false
            self.tagSession = nil
            let nfcError = error as? NFCReaderError
            if nfcError?.code != .readerSessionInvalidationErrorUserCanceled {
                self.errorMessage = error.localizedDescription
            }
        }
    }
    
    public nonisolated func tagReaderSession(_ session: NFCTagReaderSession, didDetect tags: [NFCTag]) {
        guard let firstTag = tags.first else { return }
        
        session.connect(to: firstTag) { error in
            if let error = error {
                session.invalidate(errorMessage: "Connection failed: \(error.localizedDescription)")
                Task { @MainActor in
                    self.triggerHapticFeedback(success: false)
                }
                return
            }
            
            var uidString: String? = nil
            
            switch firstTag {
            case .miFare(let miFareTag):
                let uidData = miFareTag.identifier
                uidString = uidData.map { String(format: "%02hhX", $0) }.joined()
            case .iso7816(let iso7816Tag):
                let uidData = iso7816Tag.identifier
                uidString = uidData.map { String(format: "%02hhX", $0) }.joined()
            case .iso15693(let iso15693Tag):
                let uidData = iso15693Tag.identifier
                uidString = uidData.map { String(format: "%02hhX", $0) }.joined()
            case .feliCa(let feliCaTag):
                let uidData = feliCaTag.currentIDm
                uidString = uidData.map { String(format: "%02hhX", $0) }.joined()
            @unknown default:
                break
            }
            
            if let uid = uidString, !uid.isEmpty {
                session.alertMessage = "Badge Scanned: \(uid)"
                session.invalidate()
                Task { @MainActor in
                    self.triggerHapticFeedback(success: true)
                    self.lastScannedUID = uid
                    self.isScanning = false
                    self.onScanCompletion?(uid)
                }
            } else {
                session.invalidate(errorMessage: "Could not read badge UID.")
                Task { @MainActor in
                    self.triggerHapticFeedback(success: false)
                    self.isScanning = false
                }
            }
        }
    }
}
