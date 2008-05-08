package greet

import groovy.beans.Bindable

/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: May 6, 2008
 * Time: 6:10:36 PM
 * To change this template use File | Settings | File Templates.
 */
class MockTwitterAPI {
    static private url = new File(/C:\Documents and Settings\danno.ferrin\My Documents\My Pictures\twittericonsmall.jpg/).toURL().toExternalForm()

    @Bindable String status = "\u00a0"
    def authenticatedUser = [screen_name:'shemnon', status:[text:'A bogus tweet', parent:{authenticatedUser}], name:'Test Name', location:'Not here', profile_image_url:url]
    XmlSlurper slurper = new XmlSlurper()
    def imageMap = [(url):new javax.swing.ImageIcon(new URL(url))]

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


    boolean login(def name, def password) {
        true
    }

    def getFriends(user = null) {
        return [authenticatedUser,
                [screen_name:'mockfriend1', profile_image_url:url, name:'Mock Friend 1', location:'here', status:[text:'not a tweet', user:authenticatedUser]],
                [screen_name:'bob', profile_image_url:url, name:'Robert T Lobster', location:'there', status:[text:'less than 140 characters', user:authenticatedUser]]
        ]
    }

    def getUser(user = null) {
        return authenticatedUser
    }

    def getTweets(user = null) {
        return [
                [text:'123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 ', user:authenticatedUser],
                [text:'123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789', user:authenticatedUser],
                [text:'123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789', user:authenticatedUser],
                [text:'123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789', user:authenticatedUser],
                [text:'123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789', user:authenticatedUser],
                [text:'123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789', user:authenticatedUser],
                [text:'123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789', user:authenticatedUser],
                [text:'123456789 123456789 123456789 123456789 123456789 123456789 123456789', user:authenticatedUser],
                [text:'123456789 123456789 123456789 123456789 123456789 123456789', user:authenticatedUser],
                [text:'123456789 123456789 123456789 123456789 123456789', user:authenticatedUser],
                [text:'123456789 123456789 123456789 123456789', user:authenticatedUser],
                [text:'123456789 123456789 123456789', user:authenticatedUser],
                [text:'123456789 123456789', user:authenticatedUser],
                [text:'123456789', user:authenticatedUser],
                [text:'A status text', user:authenticatedUser],
                [text:'A status text 2', user:authenticatedUser],
                [text:'A status text 3', user:authenticatedUser],
                [text:'A status text 4', user:authenticatedUser],
                [text:'A status text 5', user:authenticatedUser],
                [text:'A status text 6', user:authenticatedUser],
                [text:'A status text 7', user:authenticatedUser],

        ]
    }


    def getFriendsTimeline(user = null) {
        return getTweets()
    }


}