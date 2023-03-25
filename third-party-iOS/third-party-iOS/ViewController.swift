import AuthenticationServices
import UIKit

class ViewController: UIViewController {
    @IBOutlet private weak var appleLoginButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()

    }
}

extension ViewController {
    @IBAction func appleLoginButtonPushed(_ sender: Any) {
        loginApple()
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
