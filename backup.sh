#!/bin/bash
# backup_postgres.sh

DB_NAME="your_database"
DB_USER="postgres"
BACKUP_DIR="/backup"
DATE=$(date +%Y%m%d_%H%M%S)

# Set PGPASSWORD để tránh nhập password
export PGPASSWORD="your_password"

pg_dump -U $DB_USER -h localhost $DB_NAME > $BACKUP_DIR/postgres_backup_$DATE.sql
gzip $BACKUP_DIR/postgres_backup_$DATE.sql