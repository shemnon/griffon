package greet

import groovy.beans.Bindable

class GreetModel {

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
    
}