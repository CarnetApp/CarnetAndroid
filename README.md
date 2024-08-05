# CarnetAndroid

[<img src="https://weblate.lostpod.me/widgets/carnethtml/-/android/svg-badge.svg" alt="État de la traduction" />](https://weblate.lostpod.me/engage/carnethtml/?utm_source=widget)

Android version of Carnet

For feature request, see [Carnet Documentation](../../../CarnetDocumentation) for github or
<a href="https://framagit.org/PhieF/CarnetDocumentation"> Framagit</a>

What is Carnet ?

<a href="https://framagit.org/PhieF/CarnetDocumentation">Documentation about Carnet is available here</a>


[<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" alt="Get it on Google Play" height="60">](https://play.google.com/store/apps/details?id=com.spisoft.quicknote)	 
 [<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="60">](https://f-droid.org/app/com.spisoft.quicknote)



# Building Carnet

```
mkdir Carnet
cd Carnet
git clone git@github.com:CarnetApp/CarnetAndroid.git
git clone git@github.com:CarnetApp/Sync.git
git clone git@github.com:CarnetApp/GoogleSync.git
cd CarnetAndroid
./gradlew build
```


Even for Android, Carnet editor is written in html / Css / JS, from the CarnetElectron repository
So update with the latest editor
```
cd Carnet
git clone git@github.com:PhieF/CarnetElectron.git
cd CarnetElectron
npm install --only=dev
bash compile.sh android ../CarnetAndroid/
```

This will build and copy every JS html css files needed by the android app


## Help

You can help with translations on the dedicated platform

[Weblate](https://weblate.lostpod.me)

[<img src="https://weblate.lostpod.me/widgets/carnethtml/-/android/multi-auto.svg" alt="État de la traduction" />](https://weblate.lostpod.me/engage/carnethtml/?utm_source=widget)

## Thanks

https://github.com/PhieF/CarnetDocumentation/blob/master/THANKS.md
