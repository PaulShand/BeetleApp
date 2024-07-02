# Beetle Counter Android App

This Android application allows users to upload images of beetles and receive a count of the beetles in the image using a remote image processing service.

## Purpose

This app was designed to assist in a specific college research project. It is optimized for a particular environment and use case:

- Intended for counting beetles in images where the subjects are spread out
- Designed to work best with images featuring a predominantly white background
- May not be as effective or accurate when used in different contexts or environments

Due to these specialized requirements, the app's beetle counting functionality is most reliable when used under conditions similar to those in the research setting it was developed for.

## Features

- Select images from device storage
- Upload images to a remote server for beetle counting
- Display the original and processed images
- Show the count of beetles detected in the image

## Requirements

- Android Studio
- Kotlin
- Minimum SDK version: [Specify the minimum SDK version]
- Target SDK version: [Specify the target SDK version]

## Dependencies

- AndroidX Compose
- Coil for image loading
- OkHttp for network requests

## Setup

1. Clone the repository
2. Open the project in Android Studio
3. Sync the project with Gradle files
4. [Optional] Update the server URL in `MainActivity.kt` if needed

## Usage

1. Launch the app
2. Tap the "Pick Image" button to select an image from your device
3. The app will upload the image to the server for processing
4. Once processed, the app will display:
   - The original image
   - The processed image with beetle annotations
   - The count of beetles detected

## Permissions

The app requires the following permissions:
- `INTERNET`: To communicate with the remote server
- `READ_EXTERNAL_STORAGE`: To access images on the device

## Architecture

The app uses Jetpack Compose for the UI and follows Android's recommended architecture components.
