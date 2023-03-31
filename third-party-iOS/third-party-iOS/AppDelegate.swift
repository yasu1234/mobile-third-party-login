import TwitterKit
import LineSDK
import UIKit
import YJLoginSDK

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {

        initTwitter()
        initYahoo()
        initLine()
        return true
    }

    // MARK: UISceneSession Lifecycle

    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }

    func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
    }
}

extension AppDelegate {
    private func initTwitter() {
        // set ConsumeKey and Secret
        TWTRTwitter.sharedInstance().start(withConsumerKey: "", consumerSecret: "")
    }
    
    private func initYahoo() {
        // set Clientid and RedirectUrl
        LoginManager.shared.setup(clientId: "", redirectUri: URL(string: "")!)
    }
    
    private func initLine() {
        // set ChannelId and universalLinkURL(optional)
        LoginManager.shared.setup(channelID: "", universalLinkURL: nil)
    }
}
