#!/usr/bin/env bash

REPO_URL=`git config --get remote.origin.url`


# exit with nonzero exit code if anything fails
set -e


# initialize a new git repo inside doc dir
cd target/report
git init


# The first and only commit to this new Git repo contains all the
# files present with the commit message "Deploy to GitHub Pages".
git add .
git commit -m "Deploy to GitHub Pages"


# Force push from the current repo's master branch to the remote
# repo's gh-pages branch. (All previous history on the gh-pages branch
# will be lost, since we are overwriting it.)
git push --force --quiet $REPO_URL master:gh-pages
