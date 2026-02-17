create table if not exists users (
  id bigserial primary key,
  login varchar(255) not null unique,
  password_hash varchar(255) not null,
  created_at timestamp not null default now()
);

create table if not exists tokens (
  token varchar(64) primary key,
  user_id bigint not null references users(id) on delete cascade,
  created_at timestamp not null default now(),
  expires_at timestamp not null,
  revoked boolean not null default false
);

create table if not exists files (
  id bigserial primary key,
  user_id bigint not null references users(id) on delete cascade,
  filename varchar(255) not null,
  size_bytes bigint not null,
  storage_key varchar(255) not null,
  created_at timestamp not null default now(),
  unique(user_id, filename)
);

create index if not exists idx_tokens_user_id on tokens(user_id);
create index if not exists idx_files_user_id on files(user_id);
