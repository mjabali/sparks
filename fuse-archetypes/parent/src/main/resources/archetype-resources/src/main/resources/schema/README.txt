#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
This schema/ folder is intended to hold sample WSDL contracts and XML schema for your project. You can of course place any WSDL or Schema that is specific to a child bundle into the child's own Maven project; however, as WSDL and XSD is often reused across a number of bundles then this schema/ directory is a convenient place to store them.