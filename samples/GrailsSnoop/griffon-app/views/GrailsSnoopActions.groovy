/*
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */

import static griffon.util.GriffonApplicationUtils.*

actions {
   action( id: 'exitAction',
      name: isMacOSX ? 'Quit' : 'Exit',
      closure: controller.exit,
      mnemonic: isMacOSX ? 'Q' : 'X',
      accelerator: shortcut(isMacOSX ? 'Q' : 'X'),
   )
   action( id: 'aboutAction',
      name: 'About',
      closure: controller.about,
      mnemonic: 'B',
      accelerator: shortcut('B'),
   )

   action(id: 'largerFontAction',
      name: 'Larger Font',
      closure: controller.largerFont,
      mnemonic: 'L',
      accelerator: shortcut('shift L')
   )
   action(id: 'smallerFontAction',
      name: 'Smaller Font',
      closure: controller.smallerFont,
      mnemonic: 'S',
      accelerator: shortcut('shift S')
   )

   action( id: 'goPreviousAction',
      enabled: bind { model.historyIndex > 0 },
      closure: controller.goPrevious,
      accelerator: shortcut('P'),
      smallIcon: imageIcon("org/tango-project/tango-icon-theme/16x16/actions/go-previous.png")
   )
   action( id: 'goNextAction',
      enabled: bind { model.historyIndex < model.history.size() - 1 },
      closure: controller.goNext,
      accelerator: shortcut('N'),
      smallIcon: imageIcon("org/tango-project/tango-icon-theme/16x16/actions/go-next.png")
   )
}