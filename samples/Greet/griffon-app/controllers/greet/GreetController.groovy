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

import java.awt.Cursor
import javax.swing.JOptionPane
import javax.swing.event.HyperlinkEvent

/**
 *@author Danno Ferrin
 */
class GreetController {

    TwitterService twitterService
    GreetModel model
    GreetPanel view

    void showLoginDialog() {
        view.loginDialog.show()
    }

    void login(evt) {
        model.allowLogin = false
        doOutside {
            try {
                if (twitterService.login(view.twitterNameField.text, view.twitterPasswordField.password)) {
                    model.friends = twitterService.getFriends(twitterService.authenticatedUser)
                    model.statuses = model.friends.collect {it.status}
                    selectUser(twitterService.authenticatedUser)
                    edt {
                        model.lastUpdate = System.currentTimeMillis()
                        view.loginDialog.dispose()
                    }
                } else {
                    JOptionPane.showMessageDialog(view.loginDialog, "Login failed")
                }
            } catch (Exception e) {
                e.printStackTrace()
            } finally {
                edt {
                    model.allowLogin = true
                    model.allowSelection = true
                    model.allowTweet = true
                }
            }
        }
    }

    void updateTimeline(evt = null) {
        model.allowSelection = false
        doOutside {
            try {
                def newVal = twitterService.getFriendsTimeline(model.focusedUser).findAll {it.text =~ view.searchField.text}
                edt {
                    model.timeline = newVal
                }
            } catch (Exception e) {
                e.printStackTrace()
            } finally {
                edt {
                    model.allowSelection = true
                    model.lastUpdate = System.currentTimeMillis()
                }
            }
        }

    }

    void filterTweets(evt = null) {
        model.allowSelection =false
        doOutside {
            try {
                [statuses: { model.friends.collect {it.status}.findAll {it.text =~ view.searchField.text} },
                 timeline: { twitterService.getFriendsTimeline(model.focusedUser).findAll {it.text =~ view.searchField.text} },
                 tweets : { twitterService.getTweets(model.focusedUser).findAll {it.text =~ view.searchField.text} },
                ].each {k, v ->
                    def newVal = v()
                    edt {
                        model."$k" = newVal
                    }
                }
            } catch (Exception e) {
                e.printStackTrace()
            } finally {
                edt {
                    model.allowSelection = true
                    model.lastUpdate = System.currentTimeMillis()
                }
            }
        }
    }

    def userSelected(evt) {
        doOutside {
            selectUser(view.users.selectedItem)
        }
    }

    def selectUser(user) {
        selectUser(user.screen_name as String)
    }

    def selectUser(String screen_name) {
        model.allowSelection = false
        try {
            def newFriend = model.friends.find {it.screen_name == screen_name} ?: twitterService.getUser(screen_name)
            model.focusedUser = newFriend
            model.tweets = twitterService.getTweets(model.focusedUser).findAll {it.text =~ view.searchField.text}
            model.timeline = twitterService.getFriendsTimeline(model.focusedUser).findAll {it.text =~ view.searchField.text}
        } finally {
            edt {
                model.allowSelection = true
                model.lastUpdate = System.currentTimeMillis()
            }
        }
    }

    def tweet(evt = null) {
        model.allowTweet = false
        doOutside {
            def cleanup = { model.allowTweet = true }
            try {
                twitterService.tweet(view.tweetBox.text)
                // true story: it froze w/o the EDT call here
                cleanup = {model.allowTweet = true; view.tweetBox.text = ""}
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