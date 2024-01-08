#!/bin/bash

# Function to rename files or folders
rename_files() {
    find . -depth -regextype posix-extended -iregex '.*\/*alpha.*\.(java|xml|txt)$' | while read -r FILE ; do
        if [[ ${FILE} =~ ./*alpha ]]; then
        NEWNAME="$(echo ${FILE} | sed -e 's/alpha/sigma/I')"
        elif [[ ${FILE} =~ ./*Alpha ]]; then
        NEWNAME="$(echo ${FILE} | sed -e 's/alpha/Sigma/I')"
        # else
        #     # If neither, continue to the next file
        #     continue
        fi
        # NEWNAME="$(echo ${FILE} | sed -e 's/alpha/sigma/I')"
        echo "Renaming $FILE to $NEWNAME"
        sleep 4
        mv "${FILE}" "${NEWNAME}"
    done
}

# Call the function to start renaming
rename_files
