/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: May 6, 2008
 * Time: 3:28:45 PM
 * To change this template use File | Settings | File Templates.
 */
import static java.awt.GridBagConstraints.*
import javax.swing.SwingConstants

def tweetUser = tweet.user
if (!(tweetUser as String)) tweetUser = tweet.parent()

def tweetText = ("$tweet.text"
    // fis regex bug
    .replace('$', '&#36;')
    // change http:// links to real links
    .replaceAll(/(http:\/\/[^' \t\n\r]+)?(.?[^h]*)/, {f,l,t->l?"<a href='$l'>$l</a>$t":"$t"})
    // change @username to twitter links
    .replaceAll(/(?:@(\w*+))?([^@]*)/, {f,l,t->l?"@<a href='http://twitter.com/$l'>$l</a>$t":"$t"})
)
tweetText = "<a href='http://twitter.com/${tweetUser.screen_name}'><b>${tweetUser.screen_name}</b></a> $tweetText"

label(icon:imageIcon(new URL(tweetUser.profile_image_url as String)),
    verticalTextPosition:SwingConstants.BOTTOM,
    horizontalTextPosition:SwingConstants.CENTER,
    anchor: NORTH, insets: [3, 3, 3, 3])
editorPane(contentType:'text/html', text:tweetText,
    hyperlinkUpdate:controller.&hyperlinkPressed,
    opaque: false, editable: false, font: tweetLineFont,
    gridwidth: REMAINDER, weightx: 1.0, fill: BOTH, insets: [3, 3, 3, 3])
label(controller.api.timeAgo(tweet.created_at), font:tweetTimeFont,
    anchor:EAST, gridwidth: REMAINDER, weightx: 1.0, insets: [0, 3, 3, 3])    