"""Schema top-ups and demo seeding, run once at startup.

`Base.metadata.create_all` creates missing *tables* but never alters existing ones, so the columns
added to `users` after the table was first created have to be applied by hand. Alembic is the right
answer for a real deployment; this keeps the prototype runnable without adding a migration tool.
"""

from sqlalchemy import text
from sqlalchemy.orm import Session

from app.db.database import engine
from app.models.models import Card, Hall, Team, User
from app.schemas import TeamRole, UserRole

# Columns added to `users` after the table already existed in checked-in databases.
_USER_COLUMNS = {
    "team_id": "VARCHAR",
    "team_role": "VARCHAR",
}


def ensure_schema() -> None:
    with engine.begin() as conn:
        existing = {row[1] for row in conn.execute(text("PRAGMA table_info(users)"))}
        for column, sql_type in _USER_COLUMNS.items():
            if column not in existing:
                conn.execute(text(f"ALTER TABLE users ADD COLUMN {column} {sql_type}"))


def seed_demo_data(db: Session) -> None:
    """Creates a usable event with committees, halls, demo accounts and cards."""

    def ensure_team(name: str, capacity: int) -> Team:
        team = db.query(Team).filter(Team.name == name).first()
        if team is None:
            team = Team(name=name, capacity=capacity)
            db.add(team)
            db.flush()
        return team

    def ensure_hall(name: str, capacity: int, roles: str = "user,organizer,admin,chief_organizer,team_leader,team_member") -> Hall:
        hall = db.query(Hall).filter(Hall.name == name).first()
        if hall is None:
            hall = Hall(name=name, capacity_threshold=capacity, allowed_roles=roles, current_occupancy=0)
            db.add(hall)
            db.flush()
        return hall

    # Official Conference Committees from reference image
    csh = ensure_team("CSH - Conseil de Sécurité Historique", 30)
    cij = ensure_team("CIJ - Cour Internationale de Justice", 25)
    ecosoc = ensure_team("ECOSOC - Conseil Économique et Social", 45)
    hrc = ensure_team("HRC - Human Rights Council", 40)
    logistics = ensure_team("Logistics", 15)
    hospitality = ensure_team("Hospitality", 15)

    # Conference Halls
    ensure_hall("General Assembly - Salle Plénière", 150)
    ensure_hall("CSH Council Chamber", 30)
    ensure_hall("CIJ Courtroom", 25)
    ensure_hall("ECOSOC Hall", 45)
    ensure_hall("HRC Assembly", 40)
    ensure_hall("Cafeteria / Dining Hall", 200)

    # Demo accounts. Auth is name-only in this prototype, so these double as login credentials.
    accounts = [
        ("admin", UserRole.ADMIN, None, None, 100.0, "04A1B2C3D4"),
        ("chief", UserRole.CHIEF_ORGANIZER, None, None, 50.0, "04B2C3D4E5"),
        ("organizer", UserRole.ORGANIZER, None, None, 25.0, "04C3D4E5F6"),
        ("leader", UserRole.TEAM_LEADER, csh.id, TeamRole.LEADER, 40.0, "04D4E5F6A7"),
        ("hospitality-lead", UserRole.TEAM_LEADER, hospitality.id, TeamRole.LEADER, 30.0, "04E5F6A7B8"),
        ("press", UserRole.TEAM_MEMBER, hospitality.id, TeamRole.MEMBER, 20.0, "04F6A7B8C9"),
        ("delegate1", UserRole.USER, csh.id, TeamRole.MEMBER, 15.0, "0411223344"),
        ("delegate2", UserRole.USER, ecosoc.id, TeamRole.MEMBER, 25.0, "0455667788"),
        ("delegate3", UserRole.USER, hrc.id, TeamRole.MEMBER, 10.0, "0499AABBCC"),
        ("delegate4", UserRole.USER, cij.id, TeamRole.MEMBER, 35.0, "04DDEEFF00"),
    ]
    for name, role, team_id, team_role, balance, card_uid in accounts:
        user = db.query(User).filter(User.name == name).first()
        if user is None:
            user = User(name=name, role=role.value, token_balance=balance, email=f"{name}@mianu.org")
            db.add(user)
            db.flush()
        user.role = role.value
        user.team_id = team_id
        user.team_role = team_role.value if team_role else None
        if user.token_balance == 0.0:
            user.token_balance = balance

        # Link card if not already linked
        if card_uid:
            card = db.query(Card).filter(Card.uid == card_uid).first()
            if not card:
                card = Card(uid=card_uid, user_id=user.id)
                db.add(card)

    db.commit()
