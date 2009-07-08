package greet

import groovy.beans.Bindable

@Bindable class LoginPaneModel {
    boolean loggingIn = true
    String loginUser
    String loginPassword
    String serviceURL
}