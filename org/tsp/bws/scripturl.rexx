AppletObj=BSF('lookupBean','BWSApplet')
 SystemOutObj=BSF('lookupBean','SystemOut')
 ANode=BSF('invoke',AppletObj,'getNode','zweite')
 CALL BSF 'invoke',ANode,'setStyleAttribute','fontFamily','Verdana'
 CALL BSF 'invoke',ANode,'setStyleAttribute','backgroundColor','#ccffcc'
 CALL BSF 'invoke',ANode,'setStyleAttribute','color','#00bb00'
 CALL BSF 'invoke',ANode,'setStyleAttribute','textAlign','center'
 CALL BSF 'invoke',ANode,'setStyleAttribute','border','1px solid #000000'

-- SystemOutObj~println('argument0')

 O=.bsf~lookupbean('BWSApplet')
 ANode=O~getNode('zweite')
 ANode~setStyleAttribute('color','#00ff00')
 ANode~setStyleAttribute('textAlign','right')

 return ANode

 ::REQUIRES BSF.CLS