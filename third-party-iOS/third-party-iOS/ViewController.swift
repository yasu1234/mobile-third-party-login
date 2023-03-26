import AuthenticationServices
import GoogleSignIn
import TwitterKit
import UIKit
import YJLoginSDK

class ViewController: UIViewController {
    override func viewDidLoad() {
        super.viewDidLoad()
    }
}

extension ViewController {
    @IBAction func appleLoginButtonPushed(_ sender: Any) {
        loginApple()
    }
    
    @IBAction func twitterLoginButtonPushed(_ sender: Any) {
        loginTwitter()
    }
    
    @IBAction func googleLoginButtonPushed(_ sender: Any) {
        loginGoogle()
    }
    
    @IBAction func yahooLoginButtonPushed(_ sender: Any) {
        loginYahoo()
    }
}

extension ViewController {
    // shiuld set on Sign in with Apple in Signing and Capabilities
    private func loginApple() {
        let request = ASAuthorizationAppleIDProvider().createRequest()
        request.requestedScopes = [.fullName]
        let controller = ASAuthorizationController(authorizationRequests: [request])
        controller.delegate = self
        controller.presentationContextProvider = self
        
        controller.performRequests()
    }
    
    private func loginTwitter() {
        TWTRTwitter.sharedInstance().logIn(completion: { (session, error) in
            if let session = session {
                // use session authToken to login
                print(session.authToken)
            } else {
                print("Error")
            }
        })
    }
    
    private func loginGoogle() {
        // set GIDClientID in info.plist
        GIDSignIn.sharedInstance.signIn(withPresenting: self) {
            [weak self] signInResult, error in
            if error == nil, let token = signInResult?.user.idToken?.tokenString {
                // use token to login
                print(token)
            }
        }
    }
    
    private func loginYahoo() {        
        var bytes = [Int8].init(repeating: 0, count: 32)

        guard SecRandomCopyBytes(kSecRandomDefault, bytes.count, &bytes) == errSecSuccess else {
            return
        }

        let codeVerifier = Data(bytes: bytes, count: bytes.count)
            .base64EncodedString()
            .replacingOccurrences(of: "+", with: "-")
            .replacingOccurrences(of: "/", with: "_")
            .replacingOccurrences(of: "=", with: "")
        
        let nonce = randomNonceString()
        
        LoginManager.shared.login(
            scopes: [.openid, .profile],
            nonce: nonce,
            codeChallenge: codeVerifier
        ) {
            (result) in
            switch result {
            case .success(let loginResult):
                // use authorizationCode to login
                print(loginResult.authorizationCode)
            case .failure(let error):
                print(error)
            }
        }
    }
    
    private func randomNonceString(length: Int = 32) -> String {
        precondition(length > 0)
        let charset: Array<Character> = Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")
        var result = ""
        var remainingLength = length

        while remainingLength > 0 {
            let randoms: [UInt8] = (0 ..< 16).map { _ in
                var random: UInt8 = 0
                let errorCode = SecRandomCopyBytes(kSecRandomDefault, 1, &random)
                if errorCode != errSecSuccess {
                    print(errorCode)
                }
                return random
            }

            randoms.forEach { random in
                if remainingLength == 0 {
                    return
                }
                if random < charset.count {
                    result.append(charset[Int(random)])
                    remainingLength -= 1
                }
            }
        }
        return result
    }
}

extension ViewController: ASAuthorizationControllerDelegate, ASAuthorizationControllerPresentationContextProviding {
    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        return view.window!
    }

    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization authorization: ASAuthorization
    ) {
        guard let credential = authorization.credential as? ASAuthorizationAppleIDCredential,
              let authorizationCodeData = credential.authorizationCode,
              let authorizationCode = String(data: authorizationCodeData, encoding: .utf8) else {
                  print("don't get authorizationCode")
                  return
        }
        // use authorizationCode to login
        print(authorizationCode)
    }
    
    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError error: Error
    ) {
        print("ERROR")
        print(error)
    }
}
