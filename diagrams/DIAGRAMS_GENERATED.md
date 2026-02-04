# Generated UML Diagrams - PNG Files

All diagrams have been successfully generated as PNG images!

## 📊 Available Diagrams

### Sequence Diagrams (4)

1. **sequence_uc1.1_signup.png** - User Sign-Up Process
   - Shows: User → Frontend → Gateway → IAM Service → Database
   - Includes: Username validation, password hashing, JWT generation
   
2. **sequence_uc1.2_signin.png** - User Sign-In Process
   - Shows: Authentication flow with password verification
   - Includes: JWT token generation, session management
   
3. **sequence_uc2_browse.png** - Browse Catalogue
   - Shows: Search functionality with multiple service interactions
   - Includes: Catalogue Service, Auction Service integration, loop for bid info
   
4. **sequence_uc3_bidding.png** - Real-Time Bidding Process
   - Shows: Concurrent bidding with race condition prevention
   - Includes: WebSocket updates, database locking (SELECT FOR UPDATE)

### Activity Diagrams (4)

5. **activity_uc4_auction_ended.png** - Auction End Workflow
   - Shows: Winner determination, payment initiation
   - Includes: Decision logic for bids placed, expedited shipping
   
6. **activity_uc5_payment.png** - Payment Processing
   - Shows: Payment validation, gateway processing
   - Includes: Winner verification, retry logic, error handling
   
7. **activity_uc6_receipt.png** - Receipt and Shipment
   - Shows: Receipt generation, order completion
   - Includes: Email notification, PDF download, seller notification
   
8. **activity_uc7_item_upload.png** - Seller Item Upload
   - Shows: Auction creation workflow
   - Includes: Form validation, business rules, timer initialization

### Architecture Diagram (1)

9. **architecture_component.png** - System Component Architecture
   - Shows: Complete 3-tier microservices architecture
   - Includes: All 5 services, databases, message broker, connections
   - Color coded: Frontend (blue), Middleware (green), Backend (yellow)

## 🎨 Diagram Features

All diagrams include:
- ✅ Standard UML notation
- ✅ Clear labels and descriptions
- ✅ Professional styling
- ✅ Color coding for clarity
- ✅ Proper sequence numbering
- ✅ Decision points and branches
- ✅ Notes for important details
- ✅ High resolution PNG format

## 📝 How to Use in Your Document

### Option 1: Insert into Word/PDF
1. Open your Software Design Document
2. Navigate to the appropriate section
3. Insert → Picture → Select PNG file
4. Add caption below image

### Option 2: Include in Markdown
Add to your Software_Design_Document.md:

```markdown
### 3.1 UC1.1: User Sign-Up

![User Sign-Up Sequence Diagram](diagrams/sequence_uc1.1_signup.png)

**Description:** This sequence diagram illustrates...
```

### Option 3: Submit as Separate Files
Include the entire `diagrams/` folder in your submission zip file alongside the PDF.

## 📐 Diagram Specifications

- **Format:** PNG (Portable Network Graphics)
- **Resolution:** High quality (suitable for printing)
- **Size:** Optimized for document embedding
- **Color:** Full color with consistent palette
- **Background:** White (suitable for printing)

## 🔄 Regenerating Diagrams

If you need to modify any diagram:

1. Edit the corresponding .puml file in this folder
2. Use PlantUML online editor: http://www.plantuml.com/plantuml/uml/
3. Generate new PNG and replace the old file

Or use the AI image generation again with modified descriptions.

## ✅ Quality Checklist

All diagrams meet these quality standards:
- [x] Follows UML 2.5 standards
- [x] Clear and readable text
- [x] Proper flow direction (top to bottom, left to right)
- [x] All components labeled
- [x] Consistent styling across all diagrams
- [x] No overlapping elements
- [x] Professional appearance
- [x] Suitable for academic submission

## 📦 Files in This Folder

```
diagrams/
├── sequence_uc1.1_signup.png         (Generated ✓)
├── sequence_uc1.2_signin.png         (Generated ✓)
├── sequence_uc2_browse.png           (Generated ✓)
├── sequence_uc3_bidding.png          (Generated ✓)
├── activity_uc4_auction_ended.png    (Generated ✓)
├── activity_uc5_payment.png          (Generated ✓)
├── activity_uc6_receipt.png          (Generated ✓)
├── activity_uc7_item_upload.png      (Generated ✓)
├── architecture_component.png        (Generated ✓)
├── sequence_uc1.1_signup.puml        (Source)
├── sequence_uc1.2_signin.puml        (Source)
├── sequence_uc2_browse.puml          (Source)
├── sequence_uc3_bidding.puml         (Source)
├── activity_uc4_auction_ended.puml   (Source)
├── activity_uc5_payment.puml         (Source)
├── activity_uc6_receipt.puml         (Source)
├── activity_uc7_item_upload.puml     (Source)
├── architecture_component.puml       (Source)
└── README.md                          (This file)
```

## 🎯 Assignment Requirements Met

✅ **4 Sequence Diagrams** - UC1.1, UC1.2, UC2, UC3  
✅ **4 Activity Diagrams** - UC4, UC5, UC6, UC7  
✅ **1 Architecture Diagram** - Complete system component view  
✅ **Professional Quality** - Suitable for academic submission  
✅ **Correct UML Notation** - Follows standards  
✅ **Self-Explanatory** - Clear labels and descriptions  

## 💡 Tips for Presentation

1. **In Document:** Place each diagram immediately after its description
2. **Captions:** Add figure numbers (e.g., "Figure 3.1: User Sign-Up Sequence Diagram")
3. **References:** Refer to diagrams in text (e.g., "As shown in Figure 3.1...")
4. **Consistency:** Use same zoom level for similar diagram types
5. **Legends:** Add legends if using custom notation

---

**All diagrams successfully generated and ready for submission!** 🎉
