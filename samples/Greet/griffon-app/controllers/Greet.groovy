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
import java.awt.Cursor
import javax.swing.JOptionPane
import javax.swing.event.HyperlinkEvent

/**
 *@author Danno Ferrin
 */
class Greet {

    TwitterAPI api
    Binding builder

    @Bindable boolean allowLogin = true
    @Bindable boolean allowSelection = true
    @Bindable boolean allowTweet = true
    @Bindable def focusedUser = ""
    @Bindable def friends  = []
    @Bindable def tweets   = []
    @Bindable def timeline = []
    @Bindable def statuses = []
    @Bindable long lastUpdate = 0


    void startUp() {
        setAllowSelection(false)
        setAllowTweet(false)
        //builder.greetFrame.show()
        builder.loginDialog.show()
    }

    void login(evt) {
        setAllowLogin(false)
        SwingBuilder.doOutside(builder) {
            try {
                if (api.login(builder.twitterNameField.text, builder.twitterPasswordField.password)) {
                    setFriends(api.getFriends(api.authenticatedUser))
                    setStatuses(friends.collect {it.status})
                    selectUser(api.authenticatedUser)
                    builder.edt {
                        setLastUpdate(System.currentTimeMillis())
                        //builder.greetFrame.show()
                        builder.loginDialog.dispose()
                    }
                } else {
                    JOptionPane.showMessageDialog(builder.loginDialog, "Login failed")
                }
            } catch (Exception e) {
                e.printStackTrace()
            } finally {
                SwingBuilder.edt(builder) {
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
        SwingBuilder.doOutside(builder) {
            try {
                [Statuses: { friends.collect {it.status}.findAll {it.text =~ builder.searchField.text} },
                 Timeline: { api.getFriendsTimeline(focusedUser).findAll {it.text =~ builder.searchField.text} },
                 Tweets : { api.getTweets(focusedUser).findAll {it.text =~ builder.searchField.text} },
                ].each {k, v ->
                    def newVal = v()
                    SwingBuilder.edt(builder) {
                        "set$k"(newVal)
                    }
                }
            } catch (Exception e) {
                e.printStackTrace()
            } finally {
                SwingBuilder.edt(builder) {
                    setAllowSelection(true)
                    setAllowTweet(true)
                    setLastUpdate(System.currentTimeMillis())
                }
            }
        }
    }

    def userSelected(evt) {
        SwingBuilder.doOutside(builder) {
            selectUser(builder.users.selectedItem)
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
            setTweets(api.getTweets(focusedUser).findAll {it.text =~ builder.searchField.text})
            setTimeline(api.getFriendsTimeline(focusedUser).findAll {it.text =~ builder.searchField.text})
        } finally {
            SwingBuilder.edt(builder) {
                setAllowSelection(true)
                setAllowTweet(true)
                setLastUpdate(System.currentTimeMillis())
            }
        }
    }

    def tweet(evt = null) {
        setAllowTweet(false)
        SwingBuilder.doOutside(builder) {
            def cleanup = { setAllowTweet(true) }
            try {
                api.tweet(builder.tweetBox.text)
                // true story: it froze w/o the EDT call here
                cleanup = {setAllowTweet(true); tweetBox.text = ""}
                filterTweets()
            } finally {
                SwingBuilder.edt(builder, cleanup)
            }
        }
    }

    def hyperlinkPressed(HyperlinkEvent evt) {
        switch (evt.getEventType()) {
            case HyperlinkEvent.EventType.ACTIVATED:
                def url = evt.URL
                if (url.host =~ 'http://twitter.com/\\w+') {
                    SwingBuilder.doOutside(builder) {selectUser(url.file.substring(1))}
                } else {
                    // TODO wire in the jnlp libs into the build
                    ("javax.jnlp.ServiceManager" as Class).lookup('javax.jnlp.BasicService').showDocument(url)
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
}