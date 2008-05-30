/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: Apr 26, 2008
 * Time: 8:31:21 AM
 */
package greet

import groovy.swing.SwingBuilder
import java.awt.Cursor
import java.awt.event.ActionListener
import java.beans.PropertyChangeListener
import javax.swing.DefaultComboBoxModel
import javax.swing.JScrollPane
import javax.swing.ListCellRenderer
import javax.swing.Timer
import javax.swing.WindowConstants

actions() {
    loginAction = action(
        name: 'Login',
        enabled: bind(source: controller, sourceProperty: 'allowLogin'),
        closure: controller.&login
    )

    filterTweets = action(
        name: 'Filter',
        enabled: bind(source: controller, sourceProperty: 'allowSelection'),
        closure: controller.&filterTweets
    )

    userSelected = action(
        name: 'Select User',
        enabled: bind(source: controller, sourceProperty: 'allowSelection'),
        closure: controller.&userSelected
    )

    tweetAction = action(
        name: 'Update',
        enabled: bind(source: controller, sourceProperty: 'allowTweet'),
        closure: controller.&tweet
    )
}

tweetLineFont = new java.awt.Font("Ariel", 0, 12)
tweetTimeFont = new java.awt.Font("Ariel", 0, 9)
def userCell = label(border: emptyBorder(3))
userCellRenderer = {list, user, index, isSelected, isFocused ->
    if (user) {
        userCell.icon = controller.twitterService.imageMap[user.profile_image_url as String]
        userCell.text = "<html>$user.screen_name<br>$user.name<br>$user.location<br>"
    } else {
        userCell.icon = null
        userCell.text = null
    }
    userCell
} as ListCellRenderer

mainPanel = panel(cursor: bind(source: controller, sourceProperty: 'allowSelection',
    converter: {it ? null : Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)})
) {

    gridBagLayout()
    users = comboBox(renderer: userCellRenderer, action: userSelected,
        selectedItem:bind(source:controller, sourceProperty:'focusedUser'),
        gridwidth: REMAINDER, insets: [6, 6, 3, 6], fill: HORIZONTAL
    )
    label('Search:', insets: [3, 6, 3, 3])
    searchField = textField(columns: 20, action: filterTweets,
        insets: [3, 3, 3, 3], weightx: 1.0, fill: BOTH)
    button(action: filterTweets,
        gridwidth: REMAINDER, insets: [3, 3, 3, 6], fill:HORIZONTAL)
    tabbedPane(gridwidth: REMAINDER, weighty: 1.0, fill: BOTH) {
        scrollPane(title: 'Timeline') {
            timelinePanel = panel(new ScrollablePanel(), border:emptyBorder(3))
        }
        scrollPane(title: 'Tweets') {
            tweetPanel = panel(new ScrollablePanel(), border:emptyBorder(3))
        }
        scrollPane(title: 'Statuses') {
            statusPanel = panel(new ScrollablePanel(), border:emptyBorder(3))
        }
    }
    separator(fill: HORIZONTAL, gridwidth: REMAINDER)
    tweetBox = textField(action:tweetAction,
        fill:BOTH, weightx:1.0, insets:[3,3,1,3], gridwidth:2)
    tweetButton = button(tweetAction,
        enabled:bind(source:tweetBox, sourceProperty:'text', converter:{it.length() in  1..140}),
        gridwidth:REMAINDER, insets:[3,3,1,3])
    progressBar(value:bind(source:tweetBox, 'text', converter: {Math.min(140, it.length())}),
            string: bind(source:tweetBox, 'text', converter: {
                int count = it.length();
                ((count <= 140)
                    ? "${140 - it.length()} characters left"
                    : "${it.length() - 140} characters too many")
            }),
            minimum:0, maximum:140, stringPainted: true,
            gridwidth:REMAINDER, fill:HORIZONTAL, insets:[1,3,1,3]
    )
    separator(fill: HORIZONTAL, gridwidth: REMAINDER)
    statusLine = label(text: bind(source:controller, "statusLine"),
        gridwidth: REMAINDER, insets: [3, 6, 3, 6], anchor: WEST
    )
}

loginDialog = dialog(
    title: "Login to Greet", pack: true, resizable: false,
    defaultCloseOperation: WindowConstants.DISPOSE_ON_CLOSE,
    locationByPlatform:true)
{
    panel(border: emptyBorder(3),
        cursor: bind(source: controller, sourceProperty: 'allowLogin',
            converter: {it ? null : Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)})
    ) {
        gridBagLayout()
        label("Username:",
            anchor: EAST, insets: [3, 3, 3, 3])
        twitterNameField = textField(action:loginAction, columns: 20,
            gridwidth: REMAINDER, insets: [3, 3, 3, 3])
        label("Password:",
            anchor: EAST, insets: [3, 3, 3, 3])
        twitterPasswordField = passwordField(action:loginAction, columns: 20,
            gridwidth: REMAINDER, insets: [3, 3, 3, 3])
        panel()
        button(loginAction, defaultButton: true,
            anchor: EAST, insets: [3, 3, 3, 3])
    }
}

controller.addPropertyChangeListener("friends", {evt ->
    edt { users.model = new DefaultComboBoxModel(evt.newValue as Object[]) }
} as PropertyChangeListener)

// add data change listeners
controller.addPropertyChangeListener("lastUpdate", {evt ->
    [timeline:timelinePanel, tweets:tweetPanel, statuses:statusPanel].each {p, w ->
        edt {
            def oldName = null
            def topInset = 0
            JScrollPane parentScrollPane = w.parent.parent
            def scrollPos = parentScrollPane.verticalScrollBar.value
            if (w.componentCount) {
                oldName = w.components[0].name
                topInset = w.components[0].y
            }

            w.removeAll()
            panel(w) {
                gridBagLayout()
                controller."$p".each {
                    tweet = it
                    build(TweetLine)
                }
            }
            boolean found = false
            if (oldName && (w.components[0].name != oldName)) w.components.each { tweetLine ->
                if (tweetLine.name == oldName) {
                    found = true
                    doLater {
                        float pos = tweetLine.y - topInset
                        if (scrollPos < (tweetLine.height + tweetLine.y)) {
                            float step = pos / 500 * 18
                            Timer t
                            t = new Timer(18,  {
                                if (pos <= 0) {
                                    t.stop()
                                } else {
                                    parentScrollPane.verticalScrollBar.setValue((pos -= step) as int)
                                }
                            } as ActionListener);
                            t.repeats = true
                            t.start()
                        } else {
                            parentScrollPane.verticalScrollBar.value = scrollPos + pos
                        }
                    }
                }
            }
            if (!found)
                doLater { parentScrollPane.verticalScrollBar.value = scrollPos}
        }
    }
} as PropertyChangeListener)

def refreshTimer = new Timer(120000, filterTweets)
controller.addPropertyChangeListener("focusedUser", {refreshTimer.start()} as PropertyChangeListener)

return mainPanel