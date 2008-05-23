import greet.TwitterService

app.controllers.greet.twitterService = new TwitterService()
app.controllers.greet.setAllowSelection(false)
app.controllers.greet.setAllowTweet(false)
app.controllers.greet.showLoginDialog()
app.controllers.greet.builder.bind(source:app.controllers.greet.twitterService, sourceProperty:'status', target:app.controllers.greet, targetProperty:'statusLine')

