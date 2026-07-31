plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
}

android {
	namespace = "com.rehan.rangoli"
	compileSdk = 34

	defaultConfig {
		applicationId = "com.rehan.rangoli"
		minSdk = 24
		targetSdk = 34
		versionCode = 1
		versionName = "1.0.0"
	}

	buildTypes {
		release {
			// R8 was stripping Compose runtime + DataStore classes, which made the
			// release APK crash immediately on launch. Keep this off until the
			// keep-rules in proguard-rules.pro are verified on a real device.
			isMinifyEnabled = false
			isShrinkResources = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
		debug {
			applicationIdSuffix = ".debug"
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	kotlinOptions {
		jvmTarget = "17"
	}

	buildFeatures {
		compose = true
	}
}

dependencies {
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.activity.compose)
	implementation(libs.androidx.lifecycle.runtime.compose)
	implementation(libs.androidx.datastore.preferences)

	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.ui.graphics)
	implementation(libs.androidx.compose.ui.tooling.preview)
	implementation(libs.androidx.compose.material3)
	debugImplementation(libs.androidx.compose.ui.tooling)

	testImplementation(libs.junit)
}
