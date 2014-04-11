#!/bin/bash

if [ "${0: -4}" = 'bash' ]
then echo 'usage: ' ;
     echo "./install.sh" ;
     return 1
else
##############

unalias -a

chgpermsdv=false
sair=false
aqui=$(pwd)

if [ -z "$BDPIINSTALLDIR" ]
then echo "falta variavel de ambiente BDPIINSTALLDIR"
     sair=true
fi

if [ -d "$DATAVARBASEFOLDER" ]
then cd "$DATAVARBASEFOLDER"
     for x in   log \
                reports \
                assetstore \
                solr/oai/data \
                solr/search/data \
                solr/statistics/data \
                upload \
                var/oai/requests \
                oai/var
	     do if [ ! -d "$x" ]
		then if mkdir -p $x
		     then if [ ! $chgpermsdv ]
			  then chgpermsdv=true
			  fi
		     fi
		fi
	     done
     if $chgpermsdv
     then chgrp -R tomcat7 *
          chmod -R g+w *
     fi
else echo "falta variavel de ambiente DATAVARBASEFOLDER"
     sair=true
fi

if ! which mvn >/dev/null
then echo "maven nao encontrado"
     sair=true
fi

if ! which ant >/dev/null
then echo "ant nao encontrado"
     sair=true
fi

if $sair
then exit 1
fi

app=$(basename "$BDPIINSTALLDIR")

if [ -d  "$BDPIINSTALLDIR" ]
then cd "$BDPIINSTALLDIR"
     cd ..
     # if [ ! -d old ]
     # then mkdir old
     # fi
     # mv $app "old/$app-$(date +%s)"
     rm -rf $app
fi

cd $aqui
mvn clean
mvn -Poracle-support package
cd dspace/target/dspace-4.1-build
ant update
cd ../../..

if [ -d "$DATAVARBASEFOLDER" ]
then ln -s "$DATAVARBASEFOLDER/assetstore" "$BDPIINSTALLDIR/assetstore"
     ln -s "$DATAVARBASEFOLDER/log" "$BDPIINSTALLDIR/log"
     ln -s "$DATAVARBASEFOLDER/reports" "$BDPIINSTALLDIR/reports"
     ln -s "$DATAVARBASEFOLDER/solr/oai/data" "$BDPIINSTALLDIR/solr/oai/data"
     ln -s "$DATAVARBASEFOLDER/solr/search/data" "$BDPIINSTALLDIR/solr/search/data"
     ln -s "$DATAVARBASEFOLDER/solr/statistics/data" "$BDPIINSTALLDIR/solr/statistics/data"
     ln -s "$DATAVARBASEFOLDER/upload" "$BDPIINSTALLDIR/upload"
     ln -s "$DATAVARBASEFOLDER/var" "$BDPIINSTALLDIR/var"
fi

if [ -d "$TOMCAT_CONTEXTDIR" ]
then if [ -d "$BDPIINSTALLDIR/webapps" ]
     then cd "$BDPIINSTALLDIR/webapps"
          for w in *
          do if [ "$w" = "xmlui" ]
	    then echo "<Context docBase=\"$BDPIINSTALLDIR/webapps/$w\"/>" > "$TOMCAT_CONTEXTDIR/ROOT.xml"
	    else echo "<Context docBase=\"$BDPIINSTALLDIR/webapps/$w\"/>" > "$TOMCAT_CONTEXTDIR/$w.xml"
	    fi
          done
     fi
fi
cd $aqui

if [ "$DESENV" = "sim" ]
then
 read -t 5 -p "habilita jspui hot-deploy [S|n]? " choice ;
 case "$choice" in
  N|n )
  echo "N - ok"
  ;;
  * ) 
  source hot_deploy_jspui.sh
  hot_deploy_jspui
  echo "S - ok"
  ;;
 esac ;
fi ;

$TOMCAT_RESTART_COMMAND

echo "dspace instalado em $BDPIBASEURL, aguarde o reinicio do tomcat." ;

read -t 5 -p "ver catalina.out log [s|N]? " choice ;
case "$choice" in
 "N"|"n"|"" ) echo "N - ok" ;;
 * ) echo "S - ok" 
sudo tail -f $TOMCAT_CATALINA_OUT
 ;;
esac ;

exit 0

##############
fi
