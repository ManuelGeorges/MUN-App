from sqlalchemy import Boolean, Column, Float, ForeignKey, Integer, String, DateTime, Enum
from sqlalchemy.orm import relationship
from datetime import datetime
from app.db.database import Base
from app.schemas import MealType, TransactionType
import uuid

def generate_uuid():
    return str(uuid.uuid4())

class User(Base):
    __tablename__ = "users"

    id = Column(String, primary_key=True, default=generate_uuid)
    name = Column(String, index=True)
    email = Column(String, unique=True, index=True, nullable=True)
    phone = Column(String, index=True, nullable=True)
    token_balance = Column(Float, default=0.0)
    is_active = Column(Boolean, default=True)
    role = Column(String, default="user")  # 'admin', 'organizer', 'team_leader', 'user'

    # UserResponse has advertised these two since the schema was written, but the columns never
    # existed, so every user came back with team_id=None and could not be assigned to a team.
    team_id = Column(String, ForeignKey("teams.id"), nullable=True)
    team_role = Column(String, nullable=True)  # 'leader' | 'member'

    cards = relationship("Card", back_populates="user")
    meals = relationship("MealRecord", back_populates="user")
    transactions = relationship("TransactionRecord", back_populates="user")
    team = relationship("Team", back_populates="members", foreign_keys=[team_id])


class Team(Base):
    __tablename__ = "teams"

    id = Column(String, primary_key=True, default=generate_uuid)
    name = Column(String, index=True)
    capacity = Column(Integer, default=0)

    members = relationship("User", back_populates="team", foreign_keys=[User.team_id])

    @property
    def current_size(self) -> int:
        # Derived rather than stored. The old in-memory version kept a counter and adjusted it on
        # each move, which drifts the moment any other path touches team_id.
        return len(self.members)


class Notification(Base):
    __tablename__ = "notifications"

    id = Column(String, primary_key=True, default=generate_uuid)
    sender_id = Column(String, ForeignKey("users.id"))
    audience = Column(String, default="team")  # 'all' | 'team' | 'participant'
    team_id = Column(String, ForeignKey("teams.id"), nullable=True)
    recipient_id = Column(String, ForeignKey("users.id"), nullable=True)
    message = Column(String)
    recipient_count = Column(Integer, default=0)
    timestamp = Column(DateTime, default=datetime.utcnow)

    sender = relationship("User", foreign_keys=[sender_id])
    recipient = relationship("User", foreign_keys=[recipient_id])
    team = relationship("Team", foreign_keys=[team_id])

class Card(Base):
    __tablename__ = "cards"

    uid = Column(String, primary_key=True, index=True)
    user_id = Column(String, ForeignKey("users.id"))
    is_active = Column(Boolean, default=True)

    user = relationship("User", back_populates="cards")

class MealRecord(Base):
    __tablename__ = "meal_records"

    id = Column(String, primary_key=True, default=generate_uuid)
    user_id = Column(String, ForeignKey("users.id"))
    meal_type = Column(Enum(MealType))
    swipe_timestamp = Column(DateTime, default=datetime.utcnow)

    user = relationship("User", back_populates="meals")

class TransactionRecord(Base):
    __tablename__ = "transactions"

    id = Column(String, primary_key=True, default=generate_uuid)
    user_id = Column(String, ForeignKey("users.id"))
    amount = Column(Float)
    transaction_type = Column(Enum(TransactionType))
    description = Column(String, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)

    user = relationship("User", back_populates="transactions")


class Hall(Base):
    __tablename__ = "halls"

    id = Column(String, primary_key=True, default=generate_uuid)
    name = Column(String, index=True)
    capacity_threshold = Column(Integer, default=100)
    allowed_roles = Column(String, default="user,organizer,admin,chief_organizer,team_leader,team_member")
    current_occupancy = Column(Integer, default=0)


class AccessLog(Base):
    __tablename__ = "access_logs"

    id = Column(String, primary_key=True, default=generate_uuid)
    user_id = Column(String, ForeignKey("users.id"))
    hall_id = Column(String, ForeignKey("halls.id"))
    action = Column(String)  # 'entry' | 'exit'
    timestamp = Column(DateTime, default=datetime.utcnow)
    allowed = Column(Boolean, default=True)
    reason = Column(String, nullable=True)

    user = relationship("User")
    hall = relationship("Hall")
