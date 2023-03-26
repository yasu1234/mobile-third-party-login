import AuthenticationServices
import GoogleSignIn
import TwitterKit
import UIKit

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
