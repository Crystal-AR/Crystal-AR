# Crystal-AR

## How to get started
### Setup Tesseract
1. In the assets folder of your application, create a new directory called *tessdata*.
2. Place your Tesseract eng.traineddata file in *tessdata*.
3. Add *compile 'com.rmtheis:tess-two:5.4.1'* to your application's gradle file.

### Setup Crystal-AR
1. File > New > New Module > Import .AAR Package
2. Make sure *':crystal-ar-release'* is included in settings.gradle.
3. Add *compile project(":crystal-ar-release")* to your application's dependencies list.
4. You are now ready to start using Crystal-AR! Simply import the library in your Java file using *'import com.crystal_ar.crystal_ar.*'* and initialize it with *'new CrystalAR(context)'*.
