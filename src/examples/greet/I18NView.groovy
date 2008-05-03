/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: Apr 26, 2008
 * Time: 8:31:21 AM
 */
package greet

import java.awt.Cursor
import java.beans.PropertyChangeListener
import javax.swing.*
import groovy.swing.SwingBuilder

lookAndFeel('nimbus', 'mac', ['metal', [boldFonts: false]])

actions() {
    loginAction = action(
        name: resources.getString("loginActionName"),
        enabled: bind(source: controller, sourceProperty: 'allowLogin'),
        closure: controller.&login
    )

    filterTweets = action(
        name: resources.getString("filterActionName"),
        enabled: bind(source: controller, sourceProperty: 'allowSelection'),
        closure: controller.&filterTweets
    )

    userSelected = action(
        name: resources.getString("userSelectedActionName"),
        enabled: bind(source: controller, sourceProperty: 'allowSelection'),
        closure: controller.&userSelected
    )

    tweetAction = action(
        name: resources.getString("tweetActionName"),
        enabled: bind(source: controller, sourceProperty: 'allowTweet'),
        closure: controller.&tweet
    )
}

tweetLineFont = new java.awt.Font("Ariel", 0, 12)

tweetRenderer = listCellRenderer {
    tweetLine = panel(border: emptyBorder(3), preferredSize:[250,84]) {
        gridBagLayout()
        tweetIcon = label(verticalTextPosition:SwingConstants.BOTTOM,
            horizontalTextPosition:SwingConstants.CENTER,
            //anchor: BASELINE, insets: [3, 3, 3, 3])
            anchor: CENTER, insets: [3, 3, 3, 3])
        tweetText = editorPane(contentType:'text/html',
            opaque: false, editable: false, font: tweetLineFont,
            gridwidth: REMAINDER, weightx: 1.0, fill: BOTH, insets: [3, 3, 3, 3])
    }
    onRender {
        tweetLine.opaque = !selected
        if (value?.text as String) {
            tweetText.text = ((value.text as String)
                .replace('$', '&#36;')
                .replaceAll(/(?:@(\w*+))?([^@]*)/, {f,l,t->l?"@<a href='twitter:$l'>$l</a>$t":"$t"})
                .replaceAll(/(http:\/\/[^' \t\n\r]+)?(.?[^h]*)/, {f,l,t->l?"<a href='$l'>$l</a>$t":"$t"})
            )
            if (value?.user as String) {
                tweetIcon.icon = controller.api.imageMap[value.user.profile_image_url as String]
                tweetIcon.text = value.user.screen_name
            } else {
                tweetIcon.icon = controller.api.imageMap[value.parent().profile_image_url as String]
                tweetIcon.text = value.parent().screen_name
            }
        } else {
            tweetIcon.icon = null
            tweetIcon.text = null
            tweetText.text = null
        }
    }
}

userCellRenderer = listCellRenderer {
    userCell = label(border: emptyBorder(3))
    onRender {
        if (value) {
            userCell.icon = controller.api.imageMap[value.profile_image_url as String]
            userCell.text = "<html>$value.screen_name<br>$value.name<br>$value.location<br>"
        } else {
            userCell.icon = null
            userCell.text = null
        }
    }
}

actions { // just a wrapper to cope with my MOP abuse
    greetFrame = frame(title: resources.getString("frameTitle"),
        defaultCloseOperation: javax.swing.JFrame.DISPOSE_ON_CLOSE, size: [320, 480],
        locationByPlatform:true)
    {
        panel(cursor: bind(source: controller, sourceProperty: 'allowSelection',
            converter: {it ? null : Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)})
        ) {

            gridBagLayout()
            users = comboBox(renderer: userCellRenderer, action: userSelected,
                gridwidth: REMAINDER, insets: [6, 6, 3, 6], fill: HORIZONTAL)
            label(resources.getString('labelSearch'), insets: [3, 6, 3, 3])
            searchField = textField(columns: 20, action: filterTweets,
                insets: [3, 3, 3, 3], weightx: 1.0, fill: BOTH)
            button(action: filterTweets,
                gridwidth: REMAINDER, insets: [3, 3, 3, 6], fill:HORIZONTAL)
            tabbedPane(gridwidth: REMAINDER, weighty: 1.0, fill: BOTH) {
                scrollPane(title: resources.getString("timelineTab")) {
                    timelineList = list(visibleRowCount: 20, cellRenderer: tweetRenderer)
                }
                scrollPane(title: resources.getString("tweetsTab")) {
                    tweetList = list(visibleRowCount: 20, cellRenderer: tweetRenderer)
                }
                scrollPane(title: resources.getString("statusesTab")) {
                    statusList = list(visibleRowCount: 20, cellRenderer: tweetRenderer)
                }
                // add data change listeners
                [timeline:timelineList, tweets:tweetList, statuses:statusList].each {p, w ->
                    controller.addPropertyChangeListener(p,
                        ({evt -> w.listData = evt.newValue as Object[]} as PropertyChangeListener)
                    )
                }
            }
            separator(fill: HORIZONTAL, gridwidth: REMAINDER)
            tweetBox = textField(action:tweetAction,
                fill:BOTH, weightx:1.0, insets:[3,3,3,3], gridwidth:2)
            tweetButton = button(tweetAction,
                enabled:bind(source:tweetBox, sourceProperty:'text', converter:{it.length() < 140}),
                gridwidth:REMAINDER, insets:[3,3,3,3])
            separator(fill: HORIZONTAL, gridwidth: REMAINDER)
            statusLine = label(text: bind(source: controller.api, sourceProperty: 'status'),
                gridwidth: REMAINDER, insets: [3, 6, 3, 6], anchor: WEST
            )
        }


        loginDialog = dialog(
            title: resources.getString("loginDialogTitle"), pack: true, resizable: false,
            defaultCloseOperation: WindowConstants.DISPOSE_ON_CLOSE,
            locationByPlatform:true)
        {
            panel(border: emptyBorder(3),
                cursor: bind(source: controller, sourceProperty: 'allowLogin',
                    converter: {it ? null : Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)})
            ) {
                gridBagLayout()
                label(resources.getString("labelUsername"),
                    anchor: EAST, insets: [3, 3, 3, 3])
                twitterNameField = textField(action:loginAction, columns: 20,
                    gridwidth: REMAINDER, insets: [3, 3, 3, 3])
                label(resources.getString("labelPassword"),
                    anchor: EAST, insets: [3, 3, 3, 3])
                twitterPasswordField = passwordField(action:loginAction, columns: 20,
                    gridwidth: REMAINDER, insets: [3, 3, 3, 3])
                panel()
                button(loginAction, defaultButton: true,
                    anchor: EAST, insets: [3, 3, 3, 3])
            }
        }
    }
}

controller.addPropertyChangeListener("friends", {evt ->
    SwingBuilder.edt(binding) { users.model = new DefaultComboBoxModel(evt.newValue as Object[]) }
} as PropertyChangeListener)

new Timer(120000, filterTweets).start()
