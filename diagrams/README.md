# UML Diagrams - PlantUML Format

This folder contains all UML diagrams in PlantUML format (.puml files).

## How to Generate PNG Images

### Option 1: Online PlantUML Editor (Easiest)
1. Go to http://www.plantuml.com/plantuml/uml/
2. Copy the contents of any .puml file
3. Paste into the editor
4. Click "Submit" to generate the diagram
5. Right-click on the diagram and "Save Image As" to download PNG

### Option 2: PlantUML Desktop Application
1. Download PlantUML from https://plantuml.com/download
2. Install Java if not already installed
3. Run: `java -jar plantuml.jar *.puml`
4. PNG files will be generated in the same directory

### Option 3: VS Code Extension
1. Install "PlantUML" extension in VS Code
2. Open any .puml file
3. Press `Alt+D` to preview
4. Right-click preview and export as PNG

### Option 4: Command Line (if PlantUML is installed)
```bash
# Navigate to diagrams folder
cd c:\Users\Umer\Desktop\Ecommerce\diagrams

# Generate all diagrams
plantuml *.puml

# Or generate specific diagram
plantuml sequence_uc1.1_signup.puml
```

## Diagram Files

### Sequence Diagrams
- `sequence_uc1.1_signup.puml` - User Sign-Up
- `sequence_uc1.2_signin.puml` - User Sign-In
- `sequence_uc2_browse.puml` - Browse Catalogue
- `sequence_uc3_bidding.puml` - Bidding Process

### Activity Diagrams
- `activity_uc4_auction_ended.puml` - Auction Ended
- `activity_uc5_payment.puml` - Payment Process
- `activity_uc6_receipt.puml` - Receipt and Shipment
- `activity_uc7_item_upload.puml` - Auction Item Upload

### Architecture Diagram
- `architecture_component.puml` - System Component Architecture

## Quick Online Generation

Visit these direct links (copy .puml content and paste):

1. **PlantUML Web Server**: http://www.plantuml.com/plantuml/uml/
2. **PlantText**: https://www.planttext.com/
3. **Gravizo**: http://www.gravizo.com/

## Notes

- All diagrams follow UML 2.5 standards
- Color coding used for clarity:
  - Frontend: Light Blue
  - Middleware: Light Green
  - Backend: Light Yellow
  - Database: Light Gray
  - Messaging: Light Pink

- Diagrams include:
  - Proper actor/participant notation
  - Activation bars for active lifelines
  - Return messages
  - Notes for clarification
  - Decision points in activity diagrams
  - Loops and conditional flows

## Customization

You can edit any .puml file to:
- Change colors: `skinparam` section
- Adjust layout: spacing, direction
- Add/remove elements
- Modify text and labels

## Support

For PlantUML syntax help:
- Official Guide: https://plantuml.com/guide
- Sequence Diagrams: https://plantuml.com/sequence-diagram
- Activity Diagrams: https://plantuml.com/activity-diagram-beta
- Component Diagrams: https://plantuml.com/component-diagram
