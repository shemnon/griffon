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
import java.awt.Cursor
import javax.swing.JOptionPane
import javax.swing.event.HyperlinkEvent

/**
 *@author Danno Ferrin
 */
class GreetController {

    TwitterService twitterService
    //Binding builder // now auto-iinjected via EMC

    @Bindable boolean allowLogin = true
    @Bindable boolean allowSelection = true
    @Bindable boolean allowTweet = true
    @Bindable def focusedUser = ""
    @Bindable def friends  = []
    @Bindable def tweets   = []
    @Bindable def timeline = []
    @Bindable def statuses = []
    @Bindable long lastUpdate = 0
    @Bindable String statusLine

    void showLoginDialog() {
        builder.loginDialog.show()
    }

    void login(evt) {
        setAllowLogin(false)
        doOutside {
            try {
                if (twitterService.login(builder.twitterNameField.text, builder.twitterPasswordField.password)) {
                    setFriends(twitterService.getFriends(twitterService.authenticatedUser))
                    setStatuses(friends.collect {it.status})
                    selectUser(twitterService.authenticatedUser)
                    edt {
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
                edt {
                    setAllowLogin(true)
                    setAllowSelection(true)
                    setAllowTweet(true)
                }
            }
        }
    }

    void filterTweets(evt = null) {
        setAllowSelection(false)
        doOutside {
            try {
                [Statuses: { friends.collect {it.status}.findAll {it.text =~ builder.searchField.text} },
                 Timeline: { twitterService.getFriendsTimeline(focusedUser).findAll {it.text =~ builder.searchField.text} },
                 Tweets : { twitterService.getTweets(focusedUser).findAll {it.text =~ builder.searchField.text} },
                ].each {k, v ->
                    def newVal = v()
                    edt {
                        "set$k"(newVal)
                    }
                }
            } catch (Exception e) {
                e.printStackTrace()
            } finally {
                edt {
                    setAllowSelection(true)
                    setLastUpdate(System.currentTimeMillis())
                }
            }
        }
    }

    def userSelected(evt) {
        doOutside {
            selectUser(builder.users.selectedItem)
        }
    }

    def selectUser(user) {
        selectUser(user.screen_name as String)
    }

    def selectUser(String screen_name) {
        setAllowSelection(false)
        try {
            def newFriend = friends.find {it.screen_name == screen_name} ?: twitterService.getUser(screen_name)
            setFocusedUser(newFriend)
            setTweets(twitterService.getTweets(focusedUser).findAll {it.text =~ builder.searchField.text})
            setTimeline(twitterService.getFriendsTimeline(focusedUser).findAll {it.text =~ builder.searchField.text})
        } finally {
            edt {
                setAllowSelection(true)
                setLastUpdate(System.currentTimeMillis())
            }
        }
    }

    def tweet(evt = null) {
        setAllowTweet(false)
        doOutside {
            def cleanup = { setAllowTweet(true) }
            try {
                twitterService.tweet(builder.tweetBox.text)
                // true story: it froze w/o the EDT call here
                cleanup = {setAllowTweet(true); tweetBox.text = ""}
                filterTweets()
            } finally {
                edt(cleanup)
            }
        }
    }

    def hyperlinkPressed(HyperlinkEvent evt) {
        switch (evt.getEventType()) {
            case HyperlinkEvent.EventType.ACTIVATED:
                doOutside {
                    def url = evt.URL
                    if (url.toExternalForm() =~ 'http://twitter.com/\\w+') {
                        selectUser(url.file.substring(1))
                    } else {
                        // TODO wire in the jnlp libs into the build
                        ("javax.jnlp.ServiceManager" as Class).lookup('javax.jnlp.BasicService').showDocument(url)
                    }
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