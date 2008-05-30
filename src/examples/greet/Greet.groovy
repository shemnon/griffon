/*
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package greet

import groovy.beans.Bindable
import groovy.swing.SwingBuilder
import javax.swing.JOptionPane
import griffon.gui.GUIBuilder
import javax.swing.event.HyperlinkEvent
import java.awt.Cursor

/**
 *@author Danno Ferrin
 */
class Greet {

    TwitterAPI api
    Binding view

    @Bindable boolean allowLogin = true
    @Bindable boolean allowSelection = true
    @Bindable boolean allowTweet = true
    @Bindable def focusedUser = ""
    @Bindable def friends  = []
    @Bindable def tweets   = []
    @Bindable def timeline = []
    @Bindable def statuses = []

    void startUp() {
        setAllowSelection(false)
        setAllowTweet(false)
        view.greetFrame.show()
        view.loginDialog.show()
    }

    void login(evt) {
        setAllowLogin(false)
        view.doOutside {
            try {
                if (api.login(view.twitterNameField.text, view.twitterPasswordField.password)) {
                    setFriends(api.getFriends(api.authenticatedUser))
                    setStatuses(friends.collect {it.status})
                    selectUser(api.authenticatedUser)
                    view.greetFrame.show()
                    view.loginDialog.dispose()
                } else {
                    JOptionPane.showMessageDialog(view.loginDialog, "Login failed")
                }
            } catch (Exception e) {
                e.printStackTrace()
            } finally {
                view.edt {
                    setAllowLogin(true)
                    setAllowSelection(true)
                    setAllowTweet(true)
                }
            }
        }
    }

    void filterTweets(evt = null) {
        setAllowSelection(false)
        setAllowTweet(false)
        view.doOutside {
            try {
                setStatuses(
                    friends.collect {it.status}.findAll {it.text =~ view.searchField.text}
                )
                setTimeline(
                    api.getFriendsTimeline(focusedUser).findAll {it.text =~ view.searchField.text}
                )
                setTweets(
                    api.getTweets(focusedUser).findAll {it.text =~ view.searchField.text}
                )
            } catch (Exception e) {
                e.printStackTrace()
            } finally {
                view.edt {
                    setAllowSelection(true)
                    setAllowTweet(true)
                }
            }
        }
    }

    def userSelected(evt) {
        view.doOutside {
            selectUser(view.users.selectedItem)
        }
    }

    def selectUser(user) {
        selectUser(user.screen_name as String)
    }

    def selectUser(String screen_name) {
        setAllowSelection(false)
        setAllowTweet(false)
        try {
            def newFriend = friends.find {it.screen_name == screen_name} ?: api.getUser(screen_name)
            setFocusedUser(newFriend)
            setTweets(api.getTweets(focusedUser).findAll {it.text =~ view.searchField.text})
            setTimeline(api.getFriendsTimeline(focusedUser).findAll {it.text =~ view.searchField.text})
        } finally {
            view.edt {
                setAllowSelection(true)
                setAllowTweet(true)
            }
        }
    }

    def tweet(evt = null) {
        setAllowTweet(false)
        view.doOutside {
            try {
                api.tweet(view.tweetBox.text)
                // true story: it froze w/o the EDT call here
                view.edt {tweetBox.text = ""}
                filterTweets()
            } finally {
                setAllowTweet(true)
            }
        }
    }

    def hyperlinkPressed(HyperlinkEvent evt) {
        switch (evt.getEventType()) {
            case HyperlinkEvent.EventType.ACTIVATED:
                def url = evt.URL
                if (url.host == 'twitter.com') {
                    view.doOutside {selectUser(url.file.substring(1))}
                }
                break;
            case HyperlinkEvent.EventType.ENTERED:
                evt.getSource().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                break;
            case HyperlinkEvent.EventType.EXITED:
                evt.getSource().setCursor(null);
                break;
        }
    }

    public static void main(String[] args) {
        def model = new TwitterAPI()
        def controller = new Greet()
        def view = new SwingBuilder()

        controller.api = model
        controller.view = view

        view.controller = controller

        view.build(View)
        view.view = view

        controller.startUp()
    }
}