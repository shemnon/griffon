import java.awt.Font
import javax.swing.tree.*
import javax.swing.event.TreeSelectionListener

class GrailsSnoopController {
   def model
   def view
   def builder

   def exit = { evt ->
      app.shutdown()
   }

   def largerFont = { evt ->
      def inputArea = view.description
      def currentFont = inputArea.font
      if( currentFont.size > 40 ) return
      inputArea.font = new Font( 'Monospaced', currentFont.style, currentFont.size + 2 )
   }
   def smallerFont = { evt ->
      def inputArea = view.description
      def currentFont = inputArea.font
      if( currentFont.size < 5 ) return
      inputArea.font = new Font( 'Monospaced', currentFont.style, currentFont.size - 2 )
   }

   def about = { evt ->
      showDialog("aboutDialog")
   }

   def treeSelectionListener = { event ->
      def path = event.path
      if( path.pathCount == 3 ) {
         def node = view.topics.lastSelectedPathComponent
         if( !node ) return
         def pageId = node.userObject.pageId
         if( pageId == model.currentPageId ) return
         model.currentPageId = pageId
         browseTo( node.userObject.pageUrl )
      }
   } as TreeSelectionListener

   def topicSearch = { evt ->
      if( !model.searchText || model.searchText.size() < 2 ) return
      // the following code is based in Santhosh Kumar's search algorithm
      // http://www.jroller.com/santhosh/entry/incremental_search_jtree

      def tree = view.topics
      boolean startingFromSelection = true
      int max = tree.rowCount
      int startingRow = (tree.leadSelectionRow + 1 + max) % max
      if( startingRow < 0 || startingRow >= tree.rowCount ) {
         startingFromSelection = false
         startingRow = 0
      }

      // TODO save selection state
      // expand the whole tree otherwise search will fail
      ViewUtils.expandTree( tree )
      def path = ViewUtils.getNextMatch( tree, model.searchText, startingRow )
      // TODO restore selection state
      if( path ) {
         changeSelection( tree, path )
      }else if( startingFromSelection ) {
         path = ViewUtils.getNextMatch( tree, model.searchText, 0 )
         if( path ) changeSelection( tree, path )
      }
   }

   // probably move it to ViewUtils as well
   private void changeSelection( tree, path ) {
      // TODO collapse all non-matching categories
      doLater {
         tree.selectionPath = path
         tree.scrollPathToVisible( path )
      }
   }

   def browseTo = { url ->
      if( !model.history || model.historyIndex == model.history.size() - 1) {
         model.history << url.toString()
      }else{
         model.history = model.history[0..(model.historyIndex)]
         model.history << url.toString()
      }
      model.historyIndex = model.history.size() - 1
      doLater { model.currentPage = url instanceof URL ? url : new URL(url) }
   }

   def loadPages() {
      doOutside {
         def contents = new DefaultMutableTreeNode("Contents")
         def menuContents = new URL(model.menuUrl).text
         def lastCategory = null
         (menuContents =~ /href="(([a-zA-Z ]+)\/(.+?)\.html)"/).each { match ->
            def category = new DefaultMutableTreeNode(match[2])
            def pageNode = new PageNode( title: match[3], pageUrl: model.baseUrl+"ref/"+match[1] )
            if( lastCategory?.toString() == category.toString() ){
               lastCategory.add( new DefaultMutableTreeNode(pageNode) )
            }else{
               lastCategory = category
               category.add( new DefaultMutableTreeNode(pageNode) )
               contents.add( category )
            }
         }
         doLater {
            view.topics.model = new DefaultTreeModel(contents)
            view.topics.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
            view.topics.addTreeSelectionListener( treeSelectionListener )
         }
      }
   }

   def goPrevious = { evt ->
      model.historyIndex--
      def url = model.history[model.historyIndex]
      doLater { model.currentPage = url instanceof URL ? url : new URL(url) }
   }

   def goNext = { evt ->
      model.historyIndex++
      def url = model.history[model.historyIndex]
      doLater { model.currentPage = url instanceof URL ? url : new URL(url) }
   }

   private void showDialog( dialogName ) {
      def dialog = view."$dialogName"
      if( dialog.visible ) return
      dialog.pack()
      int x = app.appFrames[0].x + (app.appFrames[0].width - dialog.width) / 2
      int y = app.appFrames[0].y + (app.appFrames[0].height - dialog.height) / 2
      dialog.setLocation(x, y)
      dialog.show()
   }

   private void hideDialog( dialogName ) {
      def dialog = view."$dialogName"
      dialog.hide()
   }
}