# Publishing Guide for Item Landing Sound Refabricated

This guide explains how to publish **Item Landing Sound Refabricated** to **Modrinth** and **CurseForge** with the pre-formatted metadata and release files.

---

## 1. Modrinth Publication

1. Navigate to: https://modrinth.com/dashboard/create
2. Choose **Mod** as the project type.
3. Fill in the project details:
   - **Title**: `Item Landing Sound Refabricated`
   - **Slug**: `item-landing-sound-refabricated`
   - **Summary**: `Client-side Fabric mod for Minecraft 26.3 playing realistic block-based impact sounds when items hit the ground, featuring instant collision detection and zero memory leaks.`
   - **Environment**: Client only
   - **Categories**: Utility, Technology
   - **License**: MIT
   - **Source Repository**: `https://github.com/1unarea/Item-Landing-Sound-Refabricated`
   - **Issue Tracker**: `https://github.com/1unarea/Item-Landing-Sound-Refabricated/issues`
   - **Icon**: Upload `src/main/resources/assets/item_landing_sound_refabricated/icon.png`
4. Paste the markdown description from `docs/MODRINTH_PAGE.md` into the description text box.
5. Create the project.
6. Under **Versions**, click **Create Version**:
   - **Version Number**: `0.1.0-fabric-26.3`
   - **Version Title**: `0.1.0 Fabric 26.3`
   - **Release Channel**: **Beta**
   - **Supported Game Versions**: `26.3`
   - **Loaders**: `Fabric`
   - **File Upload**: Upload `build/libs/item_landing_sound_refabricated-0.1.0-fabric-26.3.jar`
   - **Dependencies**: Add `Fabric API` (Required dependency).
7. Submit the version!

---

## 2. CurseForge Publication

1. Navigate to: https://authors.curseforge.com/
2. Click **Create Project**:
   - **Game**: Minecraft
   - **Project Name**: `Item Landing Sound Refabricated`
   - **Summary**: `Client-side Fabric mod for Minecraft 26.3 playing realistic block-based impact sounds when items hit the ground, featuring instant collision detection and zero memory leaks.`
   - **Primary Category**: `Utility & QoL`
   - **Secondary Categories**: `Audio/Sound`, `Technology`
   - **Avatar / Icon**: Upload `src/main/resources/assets/item_landing_sound_refabricated/icon.png`
   - **License**: MIT
   - **Source URL**: `https://github.com/1unarea/Item-Landing-Sound-Refabricated`
   - **Issues URL**: `https://github.com/1unarea/Item-Landing-Sound-Refabricated/issues`
3. Paste the description from `docs/CURSEFORGE_PAGE.md` into the description box.
4. Submit project for review.
5. Once project is created, click **Files** -> **Upload File**:
   - **Display Name**: `Item Landing Sound Refabricated [Fabric 26.3, Beta 0.1.0]`
   - **File to Upload**: `build/libs/item_landing_sound_refabricated-0.1.0-fabric-26.3.jar`
   - **Release Type**: **Beta**
   - **Supported Minecraft Version**: `26.3`
   - **ModLoader**: `Fabric`
   - **Related Projects**: Set `Fabric API` as a Required Dependency.
6. Save and publish the file.
