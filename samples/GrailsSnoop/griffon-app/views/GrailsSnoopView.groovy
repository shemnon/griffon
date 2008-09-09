import static javax.swing.JSplitPane.*
import static javax.swing.BorderFactory.*
import static java.awt.ComponentOrientation.*
import static java.awt.BorderLayout.*
import static javax.swing.ScrollPaneConstants.*
import static java.awt.Color.*
import static ViewUtils.*
import static com.jidesoft.swing.ButtonStyle.*
import static griffon.util.GriffonApplicationUtils.*

import java.awt.Dimension
import javax.swing.event.HyperlinkEvent
import org.apache.commons.httpclient.URI as CommonsURI
import org.jdesktop.xswingx.JXSearchField

build(GrailsSnoopActions)

def hiperlinkUpdateListener = { evt ->
   if( evt.eventType == HyperlinkEvent.EventType.ACTIVATED ) {
      def uri = new CommonsURI(evt.uRL.toString(),false)
      controller.browseTo( new URL(uri.escapedURI) )
   }
}

application( title: "GrailsSnoop", size: [800,600], locationByPlatform: true,
             iconImage: imageIcon("/images/grails16.png").image ) {
   jxmenuBar( id: 'menuBar') {
      menu(text: 'File', mnemonic: 'F') {
         if( !isMacOSX ) {
            separator()
            menuItem(exitAction)
         }
      }
      menu(text: 'View', mnemonic: 'V') {
         menuItem(largerFontAction)
         menuItem(smallerFontAction)
      }
      if( !isMacOSX ) {
         menu(text: 'Help', mnemonic: 'H') {
            menuItem(aboutAction)
         }
      }
   }

   splitPane(id: 'mainPanel', dividerLocation: 210i, orientation: HORIZONTAL_SPLIT ) {
      panel( border: createShadowBorder() ) {
         borderLayout()
         textField( new JXSearchField(), id: "searchField", columns: 20, constraints: NORTH,
                  actionPerformed: controller.topicSearch )
         scrollPane( id: "sp", componentOrientation: RIGHT_TO_LEFT,
                     constraints: CENTER ) {
            jxtree( id: "topics",
                  closedIcon: imageIcon(image: ViewUtils.icons.folder),
                  openIcon: imageIcon(image: ViewUtils.icons.folderOpen),
                  leafIcon: imageIcon(image: ViewUtils.icons.entry) )
         }
      }
      panel {
         borderLayout()
         panel( border: createShadowBorder(), constraints: NORTH ) {
            borderLayout()
            textField( id: "location", columns: 30, editable: false, constraints: CENTER,
                     text: bind { model.currentPage } )
            toolBar( constraints: EAST, rollover: true, floatable: false, border: createEmptyBorder(0,0,0,0) ) {
               button( action: goPreviousAction )
               button( action: goNextAction )
            }
         }
         jxtitledPanel( title: "Description", border: createShadowBorder(), constraints: CENTER ) {
            scrollPane( horizontalScrollBarPolicy: HORIZONTAL_SCROLLBAR_NEVER,
                        border: createEmptyBorder() ) {
               editorPane( id: "description", border: createEmptyBorder(),
                           editable: false, background: WHITE, contentType: "text/html",
                           page: bind { model.currentPage },
                           hyperlinkUpdate: hiperlinkUpdateListener )
            }
         }
      }
   }
}

sp.preferredSize = new Dimension(210, sp.preferredSize.height as int)
bean( model, searchText: bind { searchField.text } )
