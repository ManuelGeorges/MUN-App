# MIANU-SM IV • Web Command Center

This folder contains the operations and admin web command center for MIANU-SM IV.

## Live Production Deployment
- **Edge URL**: `https://mianu-backend.karimshacker1234.workers.dev/admin` (also mounted at `/` and `/dashboard`)
- **API Base**: `https://mianu-backend.karimshacker1234.workers.dev/api/v1`
- **Database**: Cloudflare D1 Serverless SQL (`mianu-org-db`)

## Features
- **Real-Time Meal Monitoring**: Track breakfast, lunch, and total servings with live remaining delegate allowance meters.
- **Meal Balance Control**: Add, subtract, or set exact meal balances with instant balance synchronization.
- **Manual Meal Override Scanner**: Execute manual meal swipes by card UID.
- **Venue Locations & Room Capacity**: Live room occupancy meters with capacity warnings (At Capacity / Filling Up / Available).
- **Delegate Directory CRUD**: Add new delegates, toggle active status, assign to committees, and manage accounts.
- **Committee & Team Management**: Add new committees, manage capacities, and track assigned members.
- **Broadcast Dispatch**: Send announcements to all delegates, specific committees, or individuals.
- **Live Event Stream**: Real-time NFC door access and meal swipe event logging with type filtering.
