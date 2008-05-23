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
import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * @author Danno Ferrin
 */
class TwitterService {

    @Bindable String status = "\u00a0"
    def authenticatedUser
    XmlSlurper slurper = new XmlSlurper()
    def imageMap = [:]

    def withStatus(status, c) {
        setStatus(status)
        try {
            def o = c()
            setStatus("\u00a0")
            return o
        } catch (Throwable t) {
            setStatus("Error $status : ${t.message =~ '400'?'Rate Limit Reached':t}")
            throw t
        }
    }

    def getAPIStream(String url) {
        new StringReader(new URL(url).openStream().text)
    }

    boolean login(def name, def password) {
        withStatus("Logging in") {
            Authenticator.setDefault(
                [getPasswordAuthentication : {
                    return new PasswordAuthentication(name, password) }
                ] as Authenticator)
            authenticatedUser = getUser(name)
            return true
        }
    }

    def getFriends() {
        getFriends(authenticatedUser)
    }

    def getFriends(String user) {
        return getFriends(getUser(user))
    }

    def getFriends(user) {
        def friends = [user]
        withStatus("Loading Friends") {
            def page = 1
            def list = slurper.parse(getAPIStream("http://twitter.com/statuses/friends/${user.screen_name}.xml"))
            while (list.length) {
                list.user.collect(friends) {it}
                page++
                try {
                  list = slurper.parse("http://twitter.com/statuses/friends/${user.screen_name}.xml&page=$page")
                } catch (Exception e) { break }
            }
        }
        withStatus("Loading Friends Images") {
            return friends.each {
                loadImage(it.profile_image_url as String)
            }
        }
    }

    def getFriendsTimeline() {
        getFriendsTimeline(user)
    }

    def getFriendsTimeline(String friend) {
        getFriendsTimeline(getUser(friend))
    }

    def getFriendsTimeline(user) {
        def timeline = []
        withStatus("Loading Timeline") {
            timeline =  slurper.parse(
                    getAPIStream("http://twitter.com/statuses/friends_timeline/${user.screen_name}.xml")
                ).status.collect{it}
        }
        withStatus("Loading Timeline Images") {
            return timeline.each {
                loadImage(it.user.profile_image_url as String)
            }
        }
    }

    def getTweets() {
        return getTweets(user)
    }

    def getTweets(String friend) {
        return getTweets(getUser(frield))
    }

    def getTweets(friend) {
        def tweets = []
        withStatus("Loading Tweets") {
            tweets = slurper.parse(
                    getAPIStream("http://twitter.com/statuses/user_timeline/${friend.screen_name}.xml")
                ).status.collect{it}
        }
        withStatus("Loading Tweet Images") {
            return tweets.each {
                loadImage(it.user.profile_image_url as String)
            }
        }
    }

    def getUser(String screen_name) {
        withStatus("Loading User $screen_name") {
            if (screen_name.contains('@')) {
                return slurper.parse(
                        "http://twitter.com/users/show.xml?email=${screen_name}"
                    )
            } else {
                return slurper.parse(
                        "http://twitter.com/users/show/${screen_name}.xml"
                    )
            }
        }
    }

    def tweet(message) {
        withStatus("Tweeting") {
            def urlConnection = new URL("http://twitter.com/statuses/update.xml").openConnection()
            urlConnection.doOutput = true
            urlConnection.outputStream << "status=${URLEncoder.encode(message, 'UTF-8')}"
            return slurper.parse(urlConnection.inputStream)
        }
    }

    // no need to read these, swing seems to cache these so the EDT won't stall
    def loadImage(image) {
        if (!imageMap[image]) {
            Thread.start {imageMap[image] = new javax.swing.ImageIcon(new URL(image))}
        }
    }

    static final DateFormat twitterFormat = new SimpleDateFormat("EEE MMM dd hh:mm:ss Z yyyy")

    static def timeAgo(date) {
        if (date as String)
            return timeAgo(twitterFormat.parse(date as String))
        else
            return ""
    }

    static def timeAgo(Date d) {
        int secs = (System.currentTimeMillis() - d.getTime()) / 1000
        def dir = (secs < 0) ? "from now" : "ago"
        if (secs < 0) secs = -secs
        def parts
        switch (secs) {
            case 0..119:
                parts = [1,"minute", dir]; break
            case 120..3599:
                parts = [(secs / 60) as int, "minutes", dir]; break
            case 3600..7199:
                parts = [1, "hour", dir]; break
            case 7200..86399:
                parts = [(secs / 3600) as int, "hours", dir]; break
            case 86400..172799:
                parts = [1, "day", dir]; break
            default :
                parts = [(secs / 86400) as int, "days", dir]; break
        }
        return parts.join(" ")
    }

}
