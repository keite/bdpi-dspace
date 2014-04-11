#!/bin/bash

hot_deploy_jspui(){
   aki=$PWD
   webdir_src="$aki/dspace/modules/jspui/src/main/webapp"
   webdir_app="$BDPIINSTALLDIR/webapps/jspui"
   if [ -d "$webdir_app" ]
   then if [ -d "$webdir_src" ]
	then
	     cd $webdir_src
	     if [[ $(ls -1 | wc -l) > 0 ]]
	     then for f in $(find * -type 'f')
	          do if [ -f "$f" ]
			then if [ -f "$webdir_app/$f" ]
			     then rm $webdir_app/$f
				  ln -s $webdir_src/$f $webdir_app/$f
			     else echo "$webdir_app/$f" 'nao eh arquivo'
			     fi
			else echo "$f" 'nao eh arquivo'
			fi
		  done
	     fi
	else echo "$webdir_src" ' nao eh diretorio'
	fi
	cd $aki
   else	echo "$webdir_app" ' nao eh diretorio'
   fi
   unset aki
   return 0
}

if [ "${0: -19}" = 'hot_deploy_jspui.sh' ]
then hot_deploy_jspui
     exit 0;
fi
